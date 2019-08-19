package com.psycorp.security.token;

import com.psycorp.exception.AuthorizationException;
import com.psycorp.model.entity.User;
import com.psycorp.model.enums.ErrorEnum;
import com.psycorp.model.security.TokenEntity;
import com.psycorp.repository.UserRepository;
import com.psycorp.repository.security.TokenRepository;
import com.psycorp.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.psycorp.security.SecurityConstant.ACCESS_TOKEN_PREFIX;

/**
 * @author Vitaliy Proskura
 * @author Maryna Kontar
 */
@Component
public class TokenAuthProvider implements AuthenticationProvider {

    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final UserDetailsService userDetailsService;

    @Autowired
    public TokenAuthProvider(TokenRepository tokenRepository, UserRepository userRepository, UserDetailsServiceImpl userDetailsService) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        AuthorisationToken auth = (AuthorisationToken) authentication;
        String token = auth.getToken();
//        token = extractToken(token);
        TokenEntity tokenEntity = tokenRepository.findByToken(token)
                .orElseThrow(() -> new BadCredentialsException("Bad token"));
        User user = userRepository.findById(tokenEntity.getUserId()).orElseThrow(() ->
                new AuthorizationException("no user with token: " + token, ErrorEnum.NOT_AUTHORIZED));

        //проверка на expired date (истечение срока токена)
        if(LocalDateTime.now().isAfter(tokenEntity.getExpirationDate())) {
            // TODO refresh token
            throw new AuthorizationException("Token expire.", ErrorEnum.TOKEN_EXPIRED);
        }

        TokenPrincipal tokenPrincipal = (TokenPrincipal) userDetailsService.loadUserByUsername(user.getName());

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(tokenPrincipal, null, tokenPrincipal.getAuthorities());

        return authenticationToken;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return AuthorisationToken.class.isAssignableFrom(authentication);
    }
    private String extractToken(String authHeader) {
        return authHeader.substring(ACCESS_TOKEN_PREFIX.length() + 1);
    }
}
