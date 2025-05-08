package org.lambda.performance.native

import io.github.trueangle.knative.lambda.runtime.LambdaRuntime
import io.github.trueangle.knative.lambda.runtime.api.Context
import io.github.trueangle.knative.lambda.runtime.handler.LambdaBufferedHandler
import kotlinx.serialization.Serializable
import org.lambda.performance.common.handle

@Serializable
class RequestWrapper(
    val request: String
)

class Handler : LambdaBufferedHandler<RequestWrapper, String> {
    override suspend fun handleRequest(input: RequestWrapper, context: Context): String {
        return handle(input.request)
    }
}

fun main() = LambdaRuntime.run { Handler() }
