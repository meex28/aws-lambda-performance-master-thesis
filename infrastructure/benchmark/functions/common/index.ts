export const getFunctionArns = (): string[] => {
    const functionArns = Object.keys(process.env)
        .filter(key => key.startsWith('FUNCTION_ARN'))
        .map(key => process.env[key] as string);

    if (functionArns.length === 0) {
        throw Error('No function ARNs found in environment variables');
    }

    return functionArns;
}

export const extractFunctionNameFromArn = (arn: string): string => {
    const parts = arn.split(':');
    if (parts.length >= 7) {
        return parts[6];
    } else {
        throw Error(`Cannot extract function name from ARN: ${arn}`)
    }
}

export const getFunctionNames = (): string[] =>
    getFunctionArns().map(extractFunctionNameFromArn)

export const convertTimestampToDate = (timestamp: number): Date =>
    new Date(timestamp * 1000)