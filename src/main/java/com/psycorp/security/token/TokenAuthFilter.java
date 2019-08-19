package com.psycorp.security.token;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.psycorp.security.SecurityConstant.ACCESS_TOKEN_PREFIX;

/**
 * @author Vitaliy Proskura
 */
@Component
public class TokenAuthFilter extends AbstractAuthenticationProcessingFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(TokenAuthFilter.class);


    public TokenAuthFilter(@Autowired AuthenticationManager authenticationManager) {
        super("/**"); // authentication is required for all incoming url
        setAuthenticationManager(authenticationManager);
    }

    //Метод проверяет, должен ли фильтр попытаться выполнить аутентификацию, или передать ее выполнение дальше
    @Override
    protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        boolean a = super.requiresAuthentication(request, response);
        boolean b = StringUtils.startsWithIgnoreCase(authHeader, ACCESS_TOKEN_PREFIX);
        return a && b;
    }

    //Пытаемся определить, что за пользователь к нам стучится
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        LOGGER.info("Authentication Authorization header extracted '{}'", authHeader);

        String token = extractToken(authHeader);

        try {
            AuthorisationToken authorisationToken = AuthorisationToken.of(token);
            Authentication authenticateResult = getAuthenticationManager().authenticate(authorisationToken);

            LOGGER.info("Token Authentication success: " + authenticateResult);

            return authenticateResult;

        } catch (Exception e) {
            LOGGER.info("Authentication failure: " + e.getMessage());
            throw e;
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        super.successfulAuthentication(request, response, chain, authResult);
        // As this authentication is in HTTP header, after success we need to continue the request normally
        // and return the response as if the resource was not secured at all
        chain.doFilter(request, response);
    }

    private String extractToken(String authHeader) {
        return authHeader.substring(ACCESS_TOKEN_PREFIX.length() + 1);
    }

}
