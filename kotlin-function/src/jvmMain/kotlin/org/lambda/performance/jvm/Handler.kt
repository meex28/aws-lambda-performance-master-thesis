package org.lambda.performance.jvm

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import org.lambda.performance.common.handle

class Handler: RequestHandler<String, String> {
    override fun handleRequest(input: String, context: Context?): String {
        return handle(input)
    }
}

fun main(args: Array<String>) {
    val sampleJson = """
    {
        "fistMatrix": [
            [1, 2, 3],
            [4, 5, 6]
        ],
        "secondMatrix": [
            [7, 8],
            [9, 10],
            [11, 12]
        ]
    }
    """.trimIndent()

    handle(sampleJson)
}