package com.psycorp.exception;

import com.psycorp.model.enums.ErrorEnum;


public class ConflictException extends RestException {
    public ConflictException(String message, ErrorEnum error) {
        super(message, 409, error);
    }
}
