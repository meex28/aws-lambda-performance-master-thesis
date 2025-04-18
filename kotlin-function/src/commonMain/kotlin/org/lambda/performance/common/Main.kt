package org.lambda.performance.common

import io.konform.validation.Invalid
import io.konform.validation.Validation
import io.konform.validation.constraints.maxItems
import io.konform.validation.constraints.minItems
import io.konform.validation.onEach
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

typealias Matrix = List<List<Int>>

@Serializable
data class Request(
    val fistMatrix: Matrix,
    val secondMatrix: Matrix
)

val validateMatrix = Validation<Matrix> {
    minItems(1)

    onEach {
        minItems(1)
    }

    dynamic { rows ->
        run {
            val expectedLength = rows.first().size
            onEach {
                minItems(expectedLength)
                maxItems(expectedLength)
            }
        }
    }
}

fun validateBeforeMultiplication(matrix1: Matrix, matrix2: Matrix) {
    val validationResults = listOf(
        validateMatrix(matrix1),
        validateMatrix(matrix2)
    )

    if (validationResults.any { it is Invalid }) {
        throw IllegalArgumentException("Invalid matrix")
    }


    if (!(matrix1.size == matrix2.first().size || matrix1.first().size == matrix2.size)) {
        throw IllegalArgumentException("Matrices are not compatible")
    }
}

fun multiply(matrix1: Matrix, matrix2: Matrix): Matrix {
    validateBeforeMultiplication(
        matrix1 = matrix1,
        matrix2 = matrix2,
    )

    val resultRows = matrix1.size
    val resultCols = matrix2[0].size

    // Create result matrix initialized with zeros
    val result = MutableList(resultRows) { MutableList(resultCols) { 0 } }

    // Perform matrix multiplication
    for (i in 0 until resultRows) {
        for (j in 0 until resultCols) {
            for (k in matrix2.indices) {
                result[i][j] += matrix1[i][k] * matrix2[k][j]
            }
        }
    }

    return result
}

fun handle(input: String): String {
    val request = Json.decodeFromString<Request>(input)

    val result = try {
        multiply(
            matrix1 = request.fistMatrix,
            matrix2 = request.secondMatrix
        )
    } catch (e: IllegalArgumentException) {
        mapOf("error" to e.message)
    }

    return Json.encodeToString(result)
}
