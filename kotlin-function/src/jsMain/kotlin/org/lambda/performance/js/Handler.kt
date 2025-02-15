@file:Suppress("OPT_IN_USAGE")
@file:JsExport

package org.lambda.performance.js

import org.lambda.performance.common.handle

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

fun handler(input: Any?, context: Any?, callback: dynamic) {
    callback(
        null, LambdaResponse(
            statusCode = 200,
            body = JSON.stringify(mapOf("message" to handle()))
        ).toJson()
    )
}
