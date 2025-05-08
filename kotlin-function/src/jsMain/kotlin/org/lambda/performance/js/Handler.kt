@file:Suppress("OPT_IN_USAGE")
@file:JsExport

package org.lambda.performance.js

import org.lambda.performance.common.handle
import org.lambda.performance.js.LambdaResponse

data class LambdaResponse(
    val statusCode: Int,
    val body: String
)

fun LambdaResponse.toJson() = JSON.stringify(
    mapOf(
        "statusCode" to statusCode,
        "body" to body
    )
)

fun handler(requestWrapper: dynamic, context: Any?, callback: dynamic) {
    val result = handle(requestWrapper.request)

    callback(
        null, LambdaResponse(
            statusCode = 200,
            body = result
        ).toJson()
    )
}
