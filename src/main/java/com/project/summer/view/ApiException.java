package com.project.summer.view;

public class ApiException extends RuntimeException {
    private final String errorMessage;

    public ApiException(String errorMessage) {
        super(errorMessage);
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
