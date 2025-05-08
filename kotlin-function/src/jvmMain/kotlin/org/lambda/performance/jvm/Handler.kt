package org.lambda.performance.jvm

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import org.lambda.performance.common.Request
import org.lambda.performance.common.handle

class RequestWrapper(
    val request: String
)

class Handler : RequestHandler<RequestWrapper, String> {
    override fun handleRequest(input: RequestWrapper, context: Context?): String {
        return handle(input.request)
    }
}

//fun main(args: Array<String>) {
//    val sampleJson = """
//    {
//        "fistMatrix": [
//            [1, 2, 3],
//            [4, 5, 6]
//        ],
//        "secondMatrix": [
//            [7, 8],
//            [9, 10],
//            [11, 12]
//        ]
//    }
//    """.trimIndent()
//
//    Handler().handleRequest(
//        input = RequestWrapper(sampleJson),
//        context = null
//    )
//}
