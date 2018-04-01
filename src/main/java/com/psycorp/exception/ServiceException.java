package com.psycorp.exception;

import lombok.Getter;
import lombok.NonNull;

import java.util.Date;

@Getter
public abstract class ServiceException extends RuntimeException {
    private Date timestamp = new Date();

    @NonNull
    private String error;

    @NonNull
    private int errorCode;

    public ServiceException(@NonNull String error, @NonNull int errorCode, @NonNull String message) {
        super(message);
        this.error = error;
        this.errorCode = errorCode;
    }
}