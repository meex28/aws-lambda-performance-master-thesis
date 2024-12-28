package org.lambda.performance.native

import io.github.trueangle.knative.lambda.runtime.LambdaRuntime
import io.github.trueangle.knative.lambda.runtime.api.Context
import io.github.trueangle.knative.lambda.runtime.handler.LambdaBufferedHandler
import org.lambda.performance.common.handle

class Handler: LambdaBufferedHandler<Any, String> {
    override suspend fun handleRequest(input: Any, context: Context): String {
        return handle()
    }
}

fun main() = LambdaRuntime.run { Handler() }