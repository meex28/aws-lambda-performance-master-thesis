package org.lambda.performance.jvm

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import org.lambda.performance.common.handle

class Handler: RequestHandler<String, String> {
    override fun handleRequest(input: String, context: Context?): String {
        return handle(input)
    }
}