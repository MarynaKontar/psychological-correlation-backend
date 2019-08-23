package com.psycorp.service.security.implementation;

import com.psycorp.service.security.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Service implementation for authentication.
 * @author Vitaliy Proskura
 * @author Maryna Kontar
 */
@Service
public class AuthServiceImpl implements AuthService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceImpl.class);

    /**
     * Gets authenticated principal user.
     * If user isn't registered yet ("anonymousUser") than return null.
     * @return authenticated principal user or null if user isn't registered yet.
     */
    @Override
    public UserDetails getAuthPrincipal() {
        LOGGER.trace("getAuthPrincipal()");
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        if(authentication.getPrincipal().equals("anonymousUser")) { // if user is not registered yet, then in Authentication principal="anonymousUser"
            return null;
        }
        return (UserDetails) authentication.getPrincipal();
    }
}
