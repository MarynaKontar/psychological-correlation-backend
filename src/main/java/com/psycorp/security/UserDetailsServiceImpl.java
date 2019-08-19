package com.psycorp.security;

import com.psycorp.model.entity.User;
import com.psycorp.model.security.CredentialsEntity;
import com.psycorp.repository.UserRepository;
import com.psycorp.repository.security.CredentialsRepository;
import com.psycorp.security.token.TokenPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @author Vitaliy Proskura
 * @author Maryna Kontar
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService{
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    private final CredentialsRepository credentialsRepository;
    private final UserRepository userRepository;

    @Autowired
    public UserDetailsServiceImpl(CredentialsRepository credentialsRepository, UserRepository userRepository) {
        this.credentialsRepository = credentialsRepository;
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LOGGER.trace("loadUserByUsername({})", username);

        User user = userRepository.findUserByNameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        CredentialsEntity credential = credentialsRepository.findByUserId(user.getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        TokenPrincipal tokenPrincipal = new TokenPrincipal();
        tokenPrincipal.setId(credential.getUserId());
        tokenPrincipal.setUsername(user.getName());
        tokenPrincipal.setRole(user.getRole());
        tokenPrincipal.setPassword(credential.getPassword());
        LOGGER.trace("loadUserByUsername: tokenPrincipal: {}", tokenPrincipal);

        return tokenPrincipal;
    }
}
