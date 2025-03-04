package org.lambda.performance.native

import io.github.trueangle.knative.lambda.runtime.LambdaRuntime
import io.github.trueangle.knative.lambda.runtime.api.Context
import io.github.trueangle.knative.lambda.runtime.handler.LambdaBufferedHandler
import org.lambda.performance.common.handle

class Handler: LambdaBufferedHandler<String, String> {
    override suspend fun handleRequest(input: String, context: Context): String {
        return handle(input)
    }
}

fun main() = LambdaRuntime.run { Handler() }
