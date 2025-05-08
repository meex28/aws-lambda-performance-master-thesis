package org.lambda.performance.native

import io.github.trueangle.knative.lambda.runtime.api.Context
import kotlin.test.Test

class HandlerTest {
    
    @Test
    fun test() {
        val input = """
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
    }
}
