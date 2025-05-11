export type FunctionStartType = "cold" | "warm";

export interface InvokeFunctionMessage {
    functionArn: string
    invokeFunctionStartType: FunctionStartType
}
