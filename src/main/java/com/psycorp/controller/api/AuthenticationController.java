package com.psycorp.controller.api;

import com.psycorp.model.dto.UserAccountDto;
import com.psycorp.model.dto.UsernamePasswordDto;
import com.psycorp.model.entity.User;
import com.psycorp.model.enums.TokenType;
import com.psycorp.model.objects.UserAccount;
import com.psycorp.model.security.TokenEntity;
import com.psycorp.service.UserAccountService;
import com.psycorp.service.UserService;
import com.psycorp.service.security.AuthService;
import com.psycorp.service.security.TokenService;
import com.psycorp.сonverter.UserAccountDtoConverter;
import org.bson.types.ObjectId;
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
    private final UserService userService;
    private final TokenService tokenService;
    private final UserAccountDtoConverter userAccountDtoConverter;
    private final UserAccountService userAccountService;

    @Autowired
    public AuthenticationController(AuthService authService, UserService userService, TokenService tokenService, UserAccountDtoConverter userAccountDtoConverter,
                                    UserAccountService userAccountService) {
        this.authService = authService;
        this.userService = userService;
        this.tokenService = tokenService;
        this.userAccountDtoConverter = userAccountDtoConverter;
        this.userAccountService = userAccountService;
    }

    @PostMapping("/login")
    public ResponseEntity<UserAccountDto> login (@RequestBody @Valid UsernamePasswordDto usernamePassword) {
        String token = tokenService.generateAccessToken(usernamePassword);
        User user = tokenService.getUserByToken(token);
        UserAccount userAccount = userAccountService.getUserAccount(user);

        HttpHeaders headers = new HttpHeaders();
        headers.set("AUTHORIZATION", ACCESS_TOKEN_PREFIX + " " + token);
        return ResponseEntity.ok().headers(headers)
                .body(userAccountDtoConverter.transform(userAccount));
    }
//
    @PostMapping("/loginFriendAccount")
    public ResponseEntity<UserAccountDto> loginFriendAccount (@RequestHeader(value = "userForMatchingToken") String userForMatchingToken) {
        userForMatchingToken = userForMatchingToken.substring(ACCESS_TOKEN_PREFIX.length() + 1);
        User user = tokenService.getUserByToken(userForMatchingToken);
        UserAccount userAccount = userAccountService.getUserAccount(user);

        HttpHeaders headers = new HttpHeaders();
        headers.set("AUTHORIZATION", ACCESS_TOKEN_PREFIX + " " + userForMatchingToken);
        return ResponseEntity.ok().headers(headers)
                .body(userAccountDtoConverter.transform(userAccount));
    }

//    @PostMapping("/loginFriendAccount")
//    public ResponseEntity<UserAccountDto> loginFriendAccount (@RequestHeader(value = "userForMatchingId") ObjectId userForMatchingId) {
//        User user = userService.findById(userForMatchingId);
//        UserAccount userAccount = userAccountService.getUserAccount(user);
//        TokenEntity tokenEntity = tokenService.findByUserIdAndTokenType(userForMatchingId, TokenType.ACCESS_TOKEN);
//        if (tokenEntity == null) {
//            tokenEntity = tokenService.createUserToken(userForMatchingId, TokenType.ACCESS_TOKEN);
//        }
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("AUTHORIZATION", ACCESS_TOKEN_PREFIX + " " + tokenEntity.getToken());
//        return ResponseEntity.ok().headers(headers)
//                .body(userAccountDtoConverter.transform(userAccount));
//    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<HttpHeaders> handleException(RuntimeException ex, HttpServletRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("messageError", "Something wrong: " + ex.getMessage()
                + "; path: " + request.getServletPath());
        return ResponseEntity.badRequest().headers(httpHeaders).build();
    }
}
