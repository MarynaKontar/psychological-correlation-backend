package com.psycorp.model.enums;

import lombok.Getter;

//!!!!!!!!!!! ФАЙЛЫ ДАЛ ВИТАЛИК. ЕЩЕ НЕ РАЗБИРАЛАСЬ


@Getter
public enum ErrorEnum {
    LOGIN_USER_NOT_FOUND("exception.user.accountNotFoundException"),
    USER_DISABLED("exception.user.accountBlockedException"),
    COMPANY_SUBSCRIPTION_EXPIRED("exception.user.accountExpiredException"),
    COMPANY_DISABLED("exception.company.disabled"),
    COMPANY_NOT_FOUND("exception.company.notFound"),
    UNKNOWN_ERROR("exception.server.unknown_error"),
    TOKEN_EXPIRED("exception.auth.token_expired"),
    USER_EMAIL_NOT_FOUND("exception.user.passwordRecovery.emailNotFound"),
    NOT_AUTHORIZED("exception.auth.not_authorized_exception"),
    VALIDATION_INVALID_REQUEST("exception.bad_request"),
    USER_ALREADY_EXISTS("exception.user.already_exists"),
    NOT_ENOUGH_PERMISSIONS("exception.permission"),
    LOCATION_NOT_FOUND("exception.location.notFound"),
    INCIDENT_NOT_FOUND("exception.incident.not_found");

    private String message;
    ErrorEnum(String message) {
        this.message = message;
    }
}
