package com.mulesoft.hashicorp.consul.provider.api.exception;

public class UnknownConsulException extends Exception {
    public UnknownConsulException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }
}
