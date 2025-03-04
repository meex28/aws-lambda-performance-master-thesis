import {SNS} from 'aws-sdk';
import {getFunctionArns} from "../common";
import type {InvokeFunctionMessage} from "../common/types.ts";

const snsClient = new SNS();

export const handler = async (): Promise<any> => {
    const functionArns = getFunctionArns();
    const snsTopicArn = getSnsTopicArn();

    // Publish function ARNs to SNS topic
    await Promise.all(functionArns.map(async (functionArn) => {
        return snsClient.publish({
            TopicArn: snsTopicArn,
            Message: JSON.stringify({functionArn} as InvokeFunctionMessage)
        }).promise();
    }));

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
