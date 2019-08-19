package com.psycorp.model.enums;

import lombok.Getter;

//!!!!!!!!!!! ФАЙЛЫ ДАЛ ВИТАЛИК. ЕЩЕ НЕ РАЗБИРАЛАСЬ


/**
 * @author Maryna Kontar
 * @author Vitaliy Proskura
 */
@Getter
public enum ErrorEnum {
    LOGIN_USER_NOT_FOUND("exception.user.accountNotFoundException"),
    USER_DISABLED("exception.user.accountBlockedException"),
    UNKNOWN_ERROR("exception.server.unknown_error"),
    TOKEN_EXPIRED("exception.auth.token_expired"),
    TOKEN_NOT_FOUND("exception.auth.token_not_found"),
    USER_EMAIL_NOT_FOUND("exception.user.passwordRecovery.emailNotFound"),
    NOT_AUTHORIZED("exception.auth.not_authorized_exception"),
    VALIDATION_INVALID_REQUEST("exception.bad_request"),
    USER_ALREADY_EXISTS("exception.user.already_exists"),
    NOT_ENOUGH_PERMISSIONS("exception.permission"),
    PASSWORD_WRONG("password is wrong");

    private String message;
    ErrorEnum(String message) {
        this.message = message;
    }
}
