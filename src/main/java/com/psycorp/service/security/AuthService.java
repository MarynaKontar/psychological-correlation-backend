package com.psycorp.service.security;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * Service interface for authentication.
 * @author Vitaliy Proskura
 * @author  Maryna Kontar
 */
public interface AuthService {
    /**
     * Get authenticated principal user.
     * If user isn't registered yet ("anonymousUser") than return null.
     * @return authenticated principal user or null if user isn't registered yet.
     */
    UserDetails getAuthPrincipal();

}
