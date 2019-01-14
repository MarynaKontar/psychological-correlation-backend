package com.psycorp.controller.api;

import com.psycorp.model.dto.SimpleUserDto;
import com.psycorp.model.dto.UsernamePasswordDto;
import com.psycorp.model.entity.User;
import com.psycorp.service.ValueCompatibilityAnswersService;
import com.psycorp.service.security.AuthService;
import com.psycorp.сonverter.UserDtoConverter;
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
    private final ValueCompatibilityAnswersService valueCompatibilityAnswersService;
    private final UserDtoConverter userDtoConverter;

    @Autowired
    public AuthenticationController(AuthService authService,
                                    ValueCompatibilityAnswersService valueCompatibilityAnswersService,
                                    UserDtoConverter userDtoConverter) {
        this.authService = authService;
        this.valueCompatibilityAnswersService = valueCompatibilityAnswersService;
        this.userDtoConverter = userDtoConverter;
    }

    @PostMapping("/login")
    public ResponseEntity<SimpleUserDto> login (@RequestBody @Valid UsernamePasswordDto usernamePassword) {
        String token = authService.generateAccessToken(usernamePassword);
        User user = authService.getUserByToken(token);
        Boolean isValueCompatibilityTestPassed = valueCompatibilityAnswersService.ifTestPassed(user);

        HttpHeaders headers = new HttpHeaders();
        headers.set("AUTHORIZATION", ACCESS_TOKEN_PREFIX + " " + token);
        headers.set("isValueCompatibilityTestPassed", isValueCompatibilityTestPassed.toString());

//        return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN_PREFIX + " " + token)
//                .body(userDtoConverter.transform(user));
        return ResponseEntity.ok().headers(headers)
                .body(userDtoConverter.transform(user));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<HttpHeaders> handleException(RuntimeException ex, HttpServletRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("messageError", "Something wrong: " + ex.getMessage()
                + "; path: " + request.getServletPath());
        return ResponseEntity.badRequest().headers(httpHeaders).build();
    }
}
