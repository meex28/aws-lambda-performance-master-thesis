package org.lambda.performance.js

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

        handler(
            input = input,
            context = null,
            callback = {}
        )
    }
}
