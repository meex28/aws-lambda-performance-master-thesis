package org.lambda.performance;

import io.quarkus.runtime.annotations.RegisterForProxy;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
@RegisterForProxy
public class RequestWrapper {
    private String request;

    public RequestWrapper(String request) {
        this.request = request;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }
}
