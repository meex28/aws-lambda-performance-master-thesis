package org.lambda.performance;

import io.quarkus.runtime.annotations.RegisterForProxy;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

@RegisterForReflection
@RegisterForProxy
public class ResultResponse {
    public List<List<Integer>> result;

    public ResultResponse(List<List<Integer>> result) {
        this.result = result;
    }
}
