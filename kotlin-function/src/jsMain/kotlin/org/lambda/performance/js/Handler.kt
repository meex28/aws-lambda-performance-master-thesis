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

fun handler(input: String?, context: Any?, callback: dynamic) {
    val result = handle(input!!)

    callback(
        null, LambdaResponse(
            statusCode = 200,
            body = JSON.stringify(result)
        ).toJson()
    )
}
