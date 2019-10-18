package com.mulesoft.hashicorp.consul.provider.api.exception;

public class EmptyEnvironmentVariableException extends Exception {
    public EmptyEnvironmentVariableException(String errorMessage) {
        super(errorMessage);
    }
}
