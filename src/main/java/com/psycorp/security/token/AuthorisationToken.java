package com.psycorp.security.token;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;


public class AuthorisationToken extends UsernamePasswordAuthenticationToken {


    private AuthorisationToken(String token) {
        super(token, null);
    }

    public static AuthorisationToken of (String token) {
        return new AuthorisationToken(token);
    }

    public String getToken() {
        return (String) getPrincipal();
    }



}
