package org.lambda.performance.jvm

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.serverless.InvocationLambdaFunction
import org.lambda.performance.common.handle

@Serializable
data class RequestWrapper(
    var request: String = ""
)

val http4kApp = routes(
    "/" bind GET to { request ->
        val requestWrapper = Json.decodeFromString<RequestWrapper>(request.bodyString())
        val result = handle(requestWrapper.request)

        Response(OK).body(Json.encodeToString(result))
    }
)

@Suppress("unused")
class HelloServerlessHttp4k : InvocationLambdaFunction(http4kApp)
