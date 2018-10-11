package com.psycorp.model.enums;

import org.springframework.security.core.GrantedAuthority;

public enum  UserRole implements GrantedAuthority {
    ANONIM,
    USER,
    CORPORATE,
    ADMIN,
    DEVICE
    ;

    @Override
    public String getAuthority() {
        return "ROLE_" + name();
    }
}
