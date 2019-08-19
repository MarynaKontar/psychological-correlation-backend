package com.psycorp.model.enums;

import com.psycorp.model.security.TokenEntity;

/**
 * Type of {@link TokenEntity}
 * @author Maryna Kontar
 */
public enum TokenType {

    /* token type for accessing user for application*/
    ACCESS_TOKEN,
    /* token type for inviting another user to test */
    INVITE_TOKEN,
    /* type of token that the user receives after the expiration of the access token */
    REFRESH_TOKEN
}
