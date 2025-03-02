import {Lambda} from 'aws-sdk';
import {getFunctionArns} from "../common";

const lambda = new Lambda();

const INVOKE_COUNT = 3;

export const handler = async (): Promise<any> => {
    try {
        const functionArns = getFunctionArns()

        if (functionArns.length === 0) {
            console.error('No function ARNs found in environment variables');
            return {
                statusCode: 400,
                body: JSON.stringify({message: 'No function ARNs found in environment variables'})
            };
        }

        console.log(`Found ${functionArns.length} functions to invoke ${INVOKE_COUNT} times each`);

        const results = await Promise.all(
            functionArns.map(async (arn) => {
                console.log(`Processing function ${arn}`);
                return await invokeFunctionMultipleTimes(arn, INVOKE_COUNT);
            })
        );

        return {
            statusCode: 200,
            body: JSON.stringify({
                message: 'All functions processed successfully',
                results
            })
        };
    } catch (error) {
        console.error('Error in lambda handler:', error);
        return {
            statusCode: 500,
            body: JSON.stringify({
                message: 'Error processing functions',
                error: error instanceof Error ? error.message : String(error)
            })
        };
    }
};

async function invokeFunctionMultipleTimes(functionArn: string, count: number): Promise<any[]> {
    const results = [];
    const functionName = extractFunctionNameFromArn(functionArn);

    if (!functionName) {
        throw new Error(`Could not extract function name from ARN: ${functionArn}`);
    }

    for (let i = 0; i < count; i++) {
        // TODO: after test remove this var
        await updateFunctionEnvironmentVariable(functionName, Date.now().toString());

        // wait to be sure that env var changed
        await new Promise(resolve => setTimeout(resolve, 5000));

        const invokeResult = await lambda.invoke({
            FunctionName: functionArn,
            InvocationType: 'RequestResponse'
        }).promise();

        results.push({
            invocation: i + 1,
            statusCode: invokeResult.StatusCode,
        });
    }

    return results;
}

async function updateFunctionEnvironmentVariable(functionName: string, timestamp: string): Promise<void> {
    const configResult = await lambda.getFunctionConfiguration({
        FunctionName: functionName
    }).promise();

    const currentEnvVars = configResult.Environment?.Variables || {};

    await lambda.updateFunctionConfiguration({
        FunctionName: functionName,
        Environment: {
            Variables: {
                ...currentEnvVars,
                TIMESTAMP: timestamp
            }
        }
    }).promise();
}

function extractFunctionNameFromArn(arn: string): string | null {
    const parts = arn.split(':');
    if (parts.length >= 7) {
        return parts[6];
    }
    return null;
}
