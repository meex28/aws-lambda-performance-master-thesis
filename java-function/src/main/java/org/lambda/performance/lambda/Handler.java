package org.lambda.performance.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class Handler implements RequestHandler<Object, String> {

    @Override
    public String handleRequest(Object obj, Context context) {
        return "Hello, World!";
    }
}