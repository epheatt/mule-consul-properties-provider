package com.mulesoft.hashicorp.consul.provider.api.exception;

public class PropertyNotFoundException extends Exception {
    public PropertyNotFoundException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }
}
