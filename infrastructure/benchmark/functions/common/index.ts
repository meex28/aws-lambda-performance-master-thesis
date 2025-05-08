import {Lambda} from 'aws-sdk';

export const getFunctionArns = async (): Promise<string[]> => {
    const lambdaClient = new Lambda();

    const {Functions} = await lambdaClient.listFunctions({
        FunctionVersion: 'ALL',
        MaxItems: 1000,
    }).promise();

    return Functions!!
        .filter(f => f.FunctionName!!.startsWith('mte-tested'))
        .map(f => f.FunctionArn!!);
};

export const extractFunctionNameFromArn = (arn: string): string => {
    const parts = arn.split(':');
    if (parts.length >= 7) {
        return parts[6];
    } else {
        throw Error(`Cannot extract function name from ARN: ${arn}`)
    }
}

export const getFunctionNames = async (): Promise<string[]> =>
    (await getFunctionArns()).map(extractFunctionNameFromArn)

export const convertTimestampToDate = (timestamp: number): Date =>
    new Date(timestamp * 1000)

export const sleep = (ms: number): Promise<void> =>
    new Promise(resolve => setTimeout(resolve, ms))
