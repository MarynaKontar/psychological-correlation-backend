package com.psycorp.model.enums;

import com.psycorp.model.entity.User;
import org.springframework.security.core.GrantedAuthority;

/**
 * Role of {@link User}.
 * @author Maryna Kontar
 */
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
