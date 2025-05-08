package org.lambda.performance;

import io.quarkus.runtime.annotations.RegisterForProxy;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForProxy
@RegisterForReflection
public class ErrorResponse {
    public String message;

    public ErrorResponse(String message) {
        this.message = message;
    }
}
