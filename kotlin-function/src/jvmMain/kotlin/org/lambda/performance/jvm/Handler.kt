package org.lambda.performance.jvm

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.serverless.InvocationLambdaFunction
import org.lambda.performance.common.handle

@Serializable
data class RequestWrapper(
    var request: String = ""
)

val app: HttpHandler = { request: Request ->
    val requestWrapper = Json.decodeFromString<RequestWrapper>(request.bodyString())
    val result = handle(requestWrapper.request)

    Response(OK).body(result)
}

@Suppress("unused")
class Handler : InvocationLambdaFunction(app)
