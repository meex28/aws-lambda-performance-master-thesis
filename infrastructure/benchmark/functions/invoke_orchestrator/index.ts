import {SNS} from 'aws-sdk';
import {getFunctionArns, sleep} from "../common";
import type {InvokeFunctionMessage} from "../common/types.ts";

const snsClient = new SNS();

export const handler = async (): Promise<any> => {
    const functionArns = (await getFunctionArns()).slice(4, 7);
    const snsTopicArn = getSnsTopicArn();

    const sleepTime = 600000 / functionArns.length; // 10 minutes / number of functions

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
