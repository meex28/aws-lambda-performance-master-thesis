import {SNS} from 'aws-sdk';
import {getFunctionArns, sleep} from "../common";
import type {InvokeFunctionMessage} from "../common/types.ts";

const snsClient = new SNS();

interface FunctionFilters {
    isSnapstart: boolean;
    language: "java" | "kotlin";
}

interface Event {
    filters: FunctionFilters;
}

export const handler = async (event: Event): Promise<any> => {
    const functionArns = await getTestedFunctionsArns(event.filters);
    const snsTopicArn = getSnsTopicArn();

    const sleepTime = (13 * 60 * 1000) / functionArns.length; // 13 minutes / number of functions

    // Publish function ARNs to SNS topic
    for (const functionArn of functionArns) {
        console.log(`Publish for function arn: ${functionArn}`);
        await snsClient.publish({
            TopicArn: snsTopicArn,
            Message: JSON.stringify({functionArn} as InvokeFunctionMessage)
        }).promise();
        console.log(`Sleeping for ${sleepTime} ms`);
        await sleep(sleepTime);
    }

    return {
        statusCode: 200,
        body: JSON.stringify({
            message: 'Functions published to SNS topic',
        })
    };
};

const getSnsTopicArn = (): string => {
    const topicArn = process.env.SNS_TOPIC_ARN;
    if (!topicArn) {
        throw Error('No SNS_TOPIC_ARN found in environment variables');
    }
    return topicArn;
}

const getTestedFunctionsArns = async (filters: FunctionFilters): Promise<string[]> => {
    const allFunctionsArns = await getFunctionArns();

    const snapstartFilter = (f: string) => filters.isSnapstart
        ? f.includes('snapstart')
        : !f.includes('snapstart');
    const languageFilter = (f: string) => f.includes(filters.language);

    return allFunctionsArns.filter(f => snapstartFilter(f) && languageFilter(f));
}
