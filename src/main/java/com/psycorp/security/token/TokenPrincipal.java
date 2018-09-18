package com.psycorp.security.token;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.psycorp.model.enums.UserRole;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;


@Data
public class TokenPrincipal implements UserDetails {
    private ObjectId id;
    private String username;
    private UserRole role;
    @JsonIgnore
    private String password;


    @Override
    public Collection<UserRole> getAuthorities() {
        return Collections.singletonList(role);
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public ObjectId getId() {
        return id;
    }
}
