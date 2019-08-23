package com.psycorp.controller.api;

import com.psycorp.model.dto.UserAccountDto;
import com.psycorp.model.dto.UsernamePasswordDto;
import com.psycorp.model.entity.User;
import com.psycorp.model.objects.UserAccount;
import com.psycorp.service.UserAccountService;
import com.psycorp.service.security.TokenService;
import com.psycorp.сonverter.UserAccountDtoConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static com.psycorp.security.SecurityConstant.ACCESS_TOKEN_PREFIX;

/**
 * Rest controller for user authentication.
 * url : "/auth"
 * @author Maryna Kontar
 */
@RestController
@RequestMapping("/auth")
public class ApiAuthenticationController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiAuthenticationController.class);

    private final TokenService tokenService;
    private final UserAccountDtoConverter userAccountDtoConverter;
    private final UserAccountService userAccountService;

    @Autowired
    public ApiAuthenticationController(TokenService tokenService,
                                       UserAccountDtoConverter userAccountDtoConverter,
                                       UserAccountService userAccountService) {
        this.tokenService = tokenService;
        this.userAccountDtoConverter = userAccountDtoConverter;
        this.userAccountService = userAccountService;
    }

    /**
     * Endpoint for url ":/auth/login"
     * Login by user name (or email) and password.
     * No matter what will be requested, user name or email, authentication will still be done.
     * @param usernamePassword user name and password for authentication
     * @return ResponseEntity<UserAccountDto> with status ok and an authenticated
     * user account in response body and access token in header, if authentication is success
     */
    @PostMapping("/login")
    public ResponseEntity<UserAccountDto> login (@RequestBody @Valid UsernamePasswordDto usernamePassword) {
        LOGGER.trace("Login: '{}'",  usernamePassword);

        String token = tokenService.generateAccessToken(usernamePassword);
        User user = tokenService.getUserByToken(token);
        String refreshToken = tokenService.generateRefreshToken(user);
        UserAccount userAccount = userAccountService.getUserAccount(user);

        HttpHeaders headers = new HttpHeaders();
        headers.set("AUTHORIZATION", ACCESS_TOKEN_PREFIX + " " + token);
        headers.set("REFRESH", ACCESS_TOKEN_PREFIX + " " + token);
        LOGGER.trace("Login success: " + token);
        return ResponseEntity.ok()
                .headers(headers)
                .body(userAccountDtoConverter.transform(userAccount));
    }

    /**
     * Endpoint for url ":/auth/loginFriendAccount".
     * Use that endpoint when user took test on one device with friend
     * and want to return to friend account.
     * @param userForMatchingToken the token of the user that we will authenticate.
     * @return ResponseEntity<UserAccountDto> with status ok and an authenticated.
     * user account in response body and access token in header, if authentication is success.
     */
    @PostMapping("/loginFriendAccount")
    public ResponseEntity<UserAccountDto> loginFriendAccount (@RequestHeader(value = "userForMatchingToken") String userForMatchingToken) {
        LOGGER.trace("Login friend aAccount by token: '{}'",  userForMatchingToken);

        userForMatchingToken = userForMatchingToken.substring(ACCESS_TOKEN_PREFIX.length() + 1);
        User user = tokenService.getUserByToken(userForMatchingToken);
        UserAccount userAccount = userAccountService.getUserAccount(user);

        HttpHeaders headers = new HttpHeaders();
        headers.set("AUTHORIZATION", ACCESS_TOKEN_PREFIX + " " + userForMatchingToken);
        return ResponseEntity.ok()
                .headers(headers)
                .body(userAccountDtoConverter.transform(userAccount));
    }

    /**
     * RuntimeException handler.
     * @param ex RuntimeException for handling.
     * @param request HttpServletRequest which caused the RuntimeException.
     * @return ResponseEntity with HttpStatus BAD_REQUEST and exception message in header.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<HttpHeaders> handleException(RuntimeException ex, HttpServletRequest request) {
        LOGGER.trace("IP: {}:{}:{} : EXCEPTION: {}", request.getRemoteHost(), request.getRemotePort(), request.getRemoteUser(), ex);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("messageError", ex.getMessage());
        httpHeaders.add("path", request.getServletPath());
        return new ResponseEntity<>(httpHeaders, httpHeaders, HttpStatus.BAD_REQUEST);
    }
}
