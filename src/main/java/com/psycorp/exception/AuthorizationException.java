package com.psycorp.exception;

import com.psycorp.model.enums.ErrorEnum;

import java.util.Map;

public class AuthorizationException extends RestException {

    public AuthorizationException(String message, ErrorEnum error) {
        super(message, 403, error);
    }

    public AuthorizationException(String message, ErrorEnum error, Map<String, Object> additionalData) {
        super(message, 403, error, additionalData);
    }
}
