package com.acme.policyapi.application.exception;

public class PolicyRequestNotFoundException extends RuntimeException {
    public PolicyRequestNotFoundException(String message) {
        super(message);
    }
}