package org.lambda.performance.jvm

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import org.lambda.performance.common.handle

class Handler: RequestHandler<Any?, String> {
    override fun handleRequest(input: Any?, context: Context?): String {
        return handle()
    }
}