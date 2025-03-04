import {Lambda} from 'aws-sdk';
import {extractFunctionNameFromArn, sleep} from "../common";
import type {InvokeFunctionMessage} from "../common/types.ts";

const lambdaClient = new Lambda();

const INVOKE_COUNT = 3;

export const handler = async (event: any): Promise<any> => {
    const message: InvokeFunctionMessage = JSON.parse(event.Records[0].Sns.Message)
    const functionArn = message.functionArn!!;

    const results = await benchmarkFunction(functionArn);

    return {
        statusCode: 200,
        body: JSON.stringify({
            message: 'Function processed successfully.',
            functionName: extractFunctionNameFromArn(functionArn),
            results
        })
    };
};

async function benchmarkFunction(functionArn: string) {
    const functionName = extractFunctionNameFromArn(functionArn);

    console.log(`Processing function: ${functionName}`);

    await waitForLambdaActive(functionArn);

    const isSnapstart = functionArn.includes('snapstart');
    let results = [];

    if (isSnapstart) {
        const versionsArns = await setupSnapstartFunctionVersions(functionArn, INVOKE_COUNT);
        results = await invokeSnapstartFunction(versionsArns)
        await cleanupSnapstartFunctionVersions(functionArn);
    } else {
        results = await invokeFunctionMultipleTimes(functionArn, INVOKE_COUNT);
    }

    return results;
}

async function invokeFunctionMultipleTimes(functionArn: string, count: number, updateEnvVar: boolean = true): Promise<any[]> {
    const results = [];
    const functionName = extractFunctionNameFromArn(functionArn);

    for (let i = 0; i < count; i++) {
        // TODO: after test remove this var
        if (updateEnvVar) {
            await updateFunctionEnvironmentVariable(functionName, Date.now().toString());
        }

        // wait to be sure that env var changed
        await sleep(5000);

        let invokeResult;

        try {
            invokeResult = await lambdaClient.invoke({
                FunctionName: functionArn,
                InvocationType: 'RequestResponse'
            }).promise();
        } catch (error: any) {
            if (error.retryable) {
                i--;
                continue
            } else if (error.retryDelay) {
                i--;

                const retryDelay = Number.parseInt(error.retryDelay) + 10;
                await sleep(retryDelay);

                continue
            }
        }

        results.push({
            invocation: i + 1,
            statusCode: invokeResult!!.StatusCode,
        });
    }

    return results;
}

async function invokeSnapstartFunction(functionVersionsArns: string[]): Promise<any[]> {
    return await Promise.all(functionVersionsArns
        .map(async arn => await invokeFunctionMultipleTimes(arn, 1, false))
        .map(async r => (await r)[0])
    );
}

async function updateFunctionEnvironmentVariable(functionName: string, timestamp: string): Promise<void> {
    const configResult = await lambdaClient.getFunctionConfiguration({
        FunctionName: functionName
    }).promise();

    const currentEnvVars = configResult.Environment?.Variables || {};

    await lambdaClient.updateFunctionConfiguration({
        FunctionName: functionName,
        Environment: {
            Variables: {
                ...currentEnvVars,
                TIMESTAMP: timestamp
            }
        }
    }).promise();
}

const snapstartVersionDescriptionPrefix = "SNAPSTART VERSION:";

async function setupSnapstartFunctionVersions(functionArn: string, versionsCount: number): Promise<string[]> {
    const functionName = extractFunctionNameFromArn(functionArn);

    console.log(`Preparing ${versionsCount} versions for SnapStart function: ${functionName}`);
    const versionArns: string[] = [];

    // First invoke to ensure function is initialized
    await invokeFunctionMultipleTimes(functionArn, 1);

    for (let i = 0; i < versionsCount; i++) {
        const timestamp = Date.now().toString();
        await updateFunctionEnvironmentVariable(functionName, timestamp);

        await sleep(5000);

        const publishResult = await lambdaClient.publishVersion({
            FunctionName: functionName,
            Description: `${snapstartVersionDescriptionPrefix} Benchmark version ${i + 1} created at ${new Date().toISOString()}`
        }).promise();

        if (publishResult.Version && publishResult.FunctionArn) {
            const versionArn = publishResult.FunctionArn;
            versionArns.push(versionArn);
            console.log(`Created version ${publishResult.Version} with ARN: ${versionArn}`);

            await sleep(2000);
        }
    }

    console.log(`Created ${versionArns.length} versions for SnapStart testing`);

    await sleep(5000);

    return versionArns;
}

async function cleanupSnapstartFunctionVersions(functionArn: string): Promise<void> {
    console.log(`Cleaning up versions for function ${functionArn}`);

    const functionName = extractFunctionNameFromArn(functionArn);
    const versions = await lambdaClient.listVersionsByFunction({
        FunctionName: functionName
    }).promise();

    const versionsToDelete = versions.Versions
        ?.filter(version => version.Description?.startsWith(snapstartVersionDescriptionPrefix))
        .filter(version => version.Version)
        .map(version => version.Version)

    console.log(`Deleting ${versionsToDelete!!.length} versions for SnapStart testing`);

    for (const version of versionsToDelete!!) {
        await lambdaClient.deleteFunction({
            FunctionName: functionName,
            Qualifier: version
        }).promise();
    }
}

async function waitForLambdaActive(
    functionArn: string,
): Promise<void> {
    const maxAttempts = 30;
    const delayMs = 1000
    const functionName = extractFunctionNameFromArn(functionArn)

    let attempts = 0;
    let isActive = false;

    console.log(`Waiting for Lambda function ${functionName} to become active...`);

    while (!isActive && attempts < maxAttempts) {
        attempts++;
        try {
            const response = await lambdaClient.getFunction({FunctionName: functionName}).promise();

            const state = response.Configuration?.State;
            const lastUpdateStatus = response.Configuration?.LastUpdateStatus;

            console.log(`${functionName}: Attempt ${attempts}: Function state is "${state}", update status is "${lastUpdateStatus}"`);

            if (state === 'Active' && (lastUpdateStatus === 'Successful' || !lastUpdateStatus)) {
                isActive = true;
                console.log(`Lambda function ${functionName} is now active!`);
            } else {
                await sleep(delayMs)
            }
        } catch (error) {
            if (attempts >= maxAttempts) {
                throw new Error(`${functionName}: Maximum attempts (${maxAttempts}) reached. Lambda function ${functionName} did not become active: ${error}`);
            }

            console.warn(`${functionName}: Attempt ${attempts}: Error checking Lambda status: ${error}`);
            await sleep(delayMs);
            await new Promise(resolve => setTimeout(resolve, delayMs));
        }
    }

    if (!isActive) {
        throw new Error(`Maximum attempts (${maxAttempts}) reached. Lambda function ${functionName} did not become active.`);
    }
}
