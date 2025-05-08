package org.lambda.performance;

import io.quarkus.runtime.annotations.RegisterForProxy;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

// Model classes
@Handler.MatrixCompatibility(
        firstMatrix = "fistMatrix",
        secondMatrix = "secondMatrix",
        message = "Matrices are not compatible for multiplication"
)
@RegisterForReflection
@RegisterForProxy
public class Request {
    @NotNull(message = "First matrix cannot be null")
    @NotEmpty(message = "First matrix must not be empty")
    @Handler.RectangularMatrix
    public List<List<Integer>> fistMatrix; // keeping the typo from the original code

    @NotNull(message = "Second matrix cannot be null")
    @NotEmpty(message = "Second matrix must not be empty")
    @Handler.RectangularMatrix
    public List<List<Integer>> secondMatrix;
}
