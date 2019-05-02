package com.psycorp.controller.api;

import com.psycorp.model.dto.UserAccountDto;
import com.psycorp.model.dto.UsernamePasswordDto;
import com.psycorp.model.entity.User;
import com.psycorp.model.objects.UserAccount;
import com.psycorp.service.UserAccountService;
import com.psycorp.service.security.AuthService;
import com.psycorp.сonverter.UserAccountDtoConverter;
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
    private final UserAccountDtoConverter userAccountDtoConverter;
    private final UserAccountService userAccountService;

    @Autowired
    public AuthenticationController(AuthService authService, UserAccountDtoConverter userAccountDtoConverter,
                                    UserAccountService userAccountService) {
        this.authService = authService;
        this.userAccountDtoConverter = userAccountDtoConverter;
        this.userAccountService = userAccountService;
    }

    @PostMapping("/login")
    public ResponseEntity<UserAccountDto> login (@RequestBody @Valid UsernamePasswordDto usernamePassword) {
        String token = authService.generateAccessToken(usernamePassword);
        User user = authService.getUserByToken(token);
        UserAccount userAccount = userAccountService.getUserAccount(user);

        HttpHeaders headers = new HttpHeaders();
        headers.set("AUTHORIZATION", ACCESS_TOKEN_PREFIX + " " + token);
        return ResponseEntity.ok().headers(headers)
                .body(userAccountDtoConverter.transform(userAccount));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<HttpHeaders> handleException(RuntimeException ex, HttpServletRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("messageError", "Something wrong: " + ex.getMessage()
                + "; path: " + request.getServletPath());
        return ResponseEntity.badRequest().headers(httpHeaders).build();
    }
}
