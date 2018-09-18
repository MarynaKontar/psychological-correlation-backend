package com.psycorp.controller.api;

import com.psycorp.model.dto.UsernamePasswordDto;
import com.psycorp.service.security.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static com.psycorp.security.SecurityConstant.ACCESS_TOKEN_PREFIX;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthService authService;

    @Autowired
    public AuthenticationController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login (@RequestBody @Valid UsernamePasswordDto usernamePassword) {
        String token = authService.generateAccessToken(usernamePassword);

        return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN_PREFIX + " " + token).build();
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<HttpHeaders> handleException(RuntimeException ex, HttpServletRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("messageError", "Something wrong: " + ex.getMessage()
                + "; path: " + request.getServletPath());
        return ResponseEntity.badRequest().headers(httpHeaders).build();
    }
}
