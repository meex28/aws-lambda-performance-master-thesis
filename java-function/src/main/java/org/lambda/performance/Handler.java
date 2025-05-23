package org.lambda.performance;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Payload;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Handler implements RequestHandler<RequestWrapper, String> {

    private static final Validator validator;

    static {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Inject
    ObjectMapper objectMapper;

    @Override
    public String handleRequest(RequestWrapper input, Context context)  {
        try {
            return handle(input.getRequest(), objectMapper);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String handle(String input, ObjectMapper objectMapper) throws JsonProcessingException {
        Request request = objectMapper.readValue(input, Request.class);

        try {
            // Validate request using Bean Validation
            validateRequest(request);

            List<List<Integer>> result = multiply(request.fistMatrix, request.secondMatrix);
            return objectMapper.writeValueAsString(new ResultResponse(result));
        } catch (IllegalArgumentException e) {
            return objectMapper.writeValueAsString(new ErrorResponse(e.getMessage()));
        }
    }

    public static List<List<Integer>> multiply(List<List<Integer>> matrix1, List<List<Integer>> matrix2) {
        // With Bean Validation, we can skip this step if we've already validated the request
        // But kept for safety and to match the Kotlin implementation
        validateBeforeMultiplication(matrix1, matrix2);

        int resultRows = matrix1.size();
        int resultCols = matrix2.get(0).size();

        // Create result matrix initialized with zeros
        List<List<Integer>> result = new ArrayList<>(resultRows);
        for (int i = 0; i < resultRows; i++) {
            List<Integer> row = new ArrayList<>(resultCols);
            for (int j = 0; j < resultCols; j++) {
                row.add(0);
            }
            result.add(row);
        }

        // Perform matrix multiplication
        for (int i = 0; i < resultRows; i++) {
            for (int j = 0; j < resultCols; j++) {
                for (int k = 0; k < matrix2.size(); k++) {
                    result.get(i).set(j, result.get(i).get(j) + matrix1.get(i).get(k) * matrix2.get(k).get(j));
                }
            }
        }

        return result;
    }

    public static void validateBeforeMultiplication(List<List<Integer>> matrix1, List<List<Integer>> matrix2) {
        validateMatrix(matrix1);
        validateMatrix(matrix2);

        // Check if matrices can be multiplied
        boolean isCompatible = matrix1.get(0).size() == matrix2.size();
        if (!isCompatible) {
            throw new IllegalArgumentException("Matrices are not compatible");
        }
    }

    public static void validateMatrix(List<List<Integer>> matrix) {
        // Check if matrix has at least one row
        if (matrix == null || matrix.isEmpty()) {
            throw new IllegalArgumentException("Invalid matrix: Matrix must have at least one row");
        }

        // Check if first row has at least one column
        if (matrix.get(0) == null || matrix.get(0).isEmpty()) {
            throw new IllegalArgumentException("Invalid matrix: Matrix must have at least one column");
        }

        // Check if all rows have the same length as the first row
        int expectedLength = matrix.get(0).size();
        for (int i = 0; i < matrix.size(); i++) {
            List<Integer> row = matrix.get(i);
            if (row == null || row.size() != expectedLength) {
                throw new IllegalArgumentException("Invalid matrix: All rows must have the same length");
            }
        }
    }

    public static void validateRequest(Request request) {
        Set<ConstraintViolation<Request>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder("Request validation failed: ");
            for (ConstraintViolation<Request> violation : violations) {
                sb.append(violation.getMessage()).append("; ");
            }
            throw new IllegalArgumentException(sb.toString());
        }
    }

    // Custom annotations for validation

    /**
     * Validates that a matrix is rectangular (all rows have the same length)
     */
    @Documented
    @Constraint(validatedBy = RectangularMatrixValidator.class)
    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RectangularMatrix {
        String message() default "Matrix must be rectangular (all rows must have the same length)";

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};
    }

    /**
     * Validates that two matrices can be multiplied together
     */
    @Documented
    @Constraint(validatedBy = MatrixCompatibilityValidator.class)
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface MatrixCompatibility {
        String message() default "Matrices are not compatible for multiplication";

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};

        String firstMatrix();

        String secondMatrix();
    }

    // Custom validator implementations

    public static class RectangularMatrixValidator implements ConstraintValidator<RectangularMatrix, List<List<Integer>>> {

        @Override
        public void initialize(RectangularMatrix constraintAnnotation) {
            // No initialization needed
        }

        @Override
        public boolean isValid(List<List<Integer>> matrix, ConstraintValidatorContext context) {
            if (matrix == null || matrix.isEmpty() || matrix.get(0) == null) {
                return true; // Let @NotNull or @NotEmpty handle these cases
            }

            int expectedLength = matrix.get(0).size();

            // Check if all rows have the same number of elements
            for (List<Integer> row : matrix) {
                if (row == null || row.size() != expectedLength) {
                    return false;
                }
            }

            return true;
        }
    }

    public static class MatrixCompatibilityValidator implements ConstraintValidator<MatrixCompatibility, Object> {

        private String firstMatrixField;
        private String secondMatrixField;

        @Override
        public void initialize(MatrixCompatibility constraintAnnotation) {
            firstMatrixField = constraintAnnotation.firstMatrix();
            secondMatrixField = constraintAnnotation.secondMatrix();
        }

        @Override
        public boolean isValid(Object object, ConstraintValidatorContext context) {
            try {
                java.lang.reflect.Field firstField = object.getClass().getField(firstMatrixField);
                java.lang.reflect.Field secondField = object.getClass().getField(secondMatrixField);

                @SuppressWarnings("unchecked")
                java.util.List<java.util.List<Integer>> matrix1 =
                        (java.util.List<java.util.List<Integer>>) firstField.get(object);

                @SuppressWarnings("unchecked")
                java.util.List<java.util.List<Integer>> matrix2 =
                        (java.util.List<java.util.List<Integer>>) secondField.get(object);

                // Basic null checks - other validators will handle this
                if (matrix1 == null || matrix2 == null ||
                        matrix1.isEmpty() || matrix2.isEmpty() ||
                        matrix1.get(0) == null || matrix2.get(0) == null ||
                        matrix1.get(0).isEmpty() || matrix2.get(0).isEmpty()) {
                    return true;
                }

                // Check compatibility for multiplication
                return matrix1.get(0).size() == matrix2.size();

            } catch (Exception e) {
                // If we can't access the fields, we can't validate
                return true;
            }
        }
    }
}
