package com.mulesoft.hashicorp.consul.provider.api.exception;

public class ConsulAccessException extends Exception {
    public ConsulAccessException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }
}
