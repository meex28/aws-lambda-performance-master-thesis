package org.lambda.performance.js

import org.lambda.performance.common.handle

@OptIn(ExperimentalJsExport::class)
@JsExport
fun handler(input: Any?, context: Any?): String {
    return handle()
}