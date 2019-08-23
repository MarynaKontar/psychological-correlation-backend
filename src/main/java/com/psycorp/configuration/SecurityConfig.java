package com.psycorp.configuration;

import com.psycorp.security.token.TokenAuthFilter;
import com.psycorp.security.token.TokenAuthProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Collections;

/**
 * Security configuration.
 * @author Vitaliy Proskura
 * @author Maryna Kontar
 */
@Configuration
@EnableWebSecurity
// disable configuration out of the box
// We inherit our configuration class from WebSecurityConfigurerAdapter so that we don’t need to configure everything,
// but only configure (overwrite) the settings that we need
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final TokenAuthProvider tokenAuthProvider;

    @Autowired
    public SecurityConfig(TokenAuthProvider tokenAuthProvider) {
        this.tokenAuthProvider = tokenAuthProvider;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                //add our custom filter before all spring secure filters
                .addFilterBefore(tokenAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/auth/login").permitAll()
                .antMatchers(HttpMethod.POST,"/registration").permitAll()
                .antMatchers(HttpMethod.POST,"/test/goal").permitAll()
                .antMatchers(HttpMethod.GET,"/test/initTest").permitAll()
                .anyRequest().authenticated();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(Collections.singletonList(tokenAuthProvider));
    }

    // Create filter as separate bean so that it is present in the Spring context.
    // So that in the future you can work with him and customize
    @Bean
    public TokenAuthFilter tokenAuthFilter () throws Exception {
        TokenAuthFilter tokenAuthFilter = new TokenAuthFilter(authenticationManager());
        tokenAuthFilter.setAuthenticationManager(authenticationManager());
        tokenAuthFilter.setAuthenticationSuccessHandler((request, response, authentication) -> {});
        return tokenAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
