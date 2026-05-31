package com.hotelerp.userservice.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final String errorCode;
    private final String details;

    public CustomException(String message, String errorCode, String details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }
}
