package com.psycorp.security;

import com.psycorp.model.entity.User;
import com.psycorp.model.security.CredentialsEntity;
import com.psycorp.repository.UserRepository;
import com.psycorp.repository.security.CredentialsRepository;
import com.psycorp.security.token.TokenPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class UserDetailsServiceImpl implements UserDetailsService{

    private final CredentialsRepository credentialsRepository;
    private final UserRepository userRepository;

    @Autowired
    public UserDetailsServiceImpl(CredentialsRepository credentialsRepository, UserRepository userRepository) {
        this.credentialsRepository = credentialsRepository;
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findUserByNameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        CredentialsEntity credential = credentialsRepository.findByUserId(user.getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        TokenPrincipal tokenPrincipal = new TokenPrincipal();
        tokenPrincipal.setId(credential.getUserId());
        tokenPrincipal.setUsername(user.getName());
        tokenPrincipal.setRole(user.getRole());
        tokenPrincipal.setPassword(credential.getPassword());

        return tokenPrincipal;
    }
}
