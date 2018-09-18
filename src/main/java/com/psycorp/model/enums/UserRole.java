package com.psycorp.model.enums;

import org.springframework.security.core.GrantedAuthority;

public enum  UserRole implements GrantedAuthority {
    USER,
    ANONIM,
    ADMIN,
    DEVICE
    ;

    @Override
    public String getAuthority() {
        return "ROLE_" + name();
    }
}
