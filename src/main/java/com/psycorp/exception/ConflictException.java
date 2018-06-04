package com.psycorp.exception;

import com.psycorp.model.enums.ErrorEnum;

//!!!!!!!!!!! ФАЙЛЫ ДАЛ ВИТАЛИК. ЕЩЕ НЕ РАЗБИРАЛАСЬ

public class ConflictException extends RestException {
    public ConflictException(String message, ErrorEnum error) {
        super(message, 409, error);
    }
}
