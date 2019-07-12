package com.psycorp.controller.api;

import com.psycorp.model.dto.ChangePasswordDto;
import com.psycorp.model.dto.CredentialsDto;
import com.psycorp.model.dto.UserAccountDto;
import com.psycorp.model.enums.TokenType;
import com.psycorp.model.objects.UserAccount;
import com.psycorp.model.security.TokenEntity;
import com.psycorp.service.CredentialsService;
import com.psycorp.service.security.TokenService;
import com.psycorp.сonverter.CredentialsDtoConverter;
import com.psycorp.сonverter.UserAccountDtoConverter;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static com.psycorp.security.SecurityConstant.ACCESS_TOKEN_PREFIX;

@RestController
@RequestMapping("/registration")
@PropertySource("classpath:errormessages.properties")
public class ApiRegistrationController {

    private final CredentialsService credentialsService;
    private final TokenService tokenService;
    private final UserAccountDtoConverter userAccountDtoConverter;
    private final CredentialsDtoConverter credentialsDtoConverter;

    public ApiRegistrationController(CredentialsService credentialsService, TokenService tokenService,
                                     UserAccountDtoConverter userAccountDtoConverter,
                                     CredentialsDtoConverter credentialsDtoConverter) {
        this.credentialsService = credentialsService;
        this.tokenService = tokenService;
        this.userAccountDtoConverter = userAccountDtoConverter;
        this.credentialsDtoConverter = credentialsDtoConverter;
    }

    @PostMapping
    public ResponseEntity<UserAccountDto> register(@RequestBody @NotNull @Valid CredentialsDto credentialsDto,
                                                   @RequestHeader(value = "Authorization", required = false) String token) {

        UserAccount userAccount = credentialsService.save(credentialsDtoConverter.transform(credentialsDto), token);
        token = tokenService.getTokenForRegisteredUser(token, userAccount.getUser().getId());
        HttpHeaders headers = new HttpHeaders();
        headers.set("AUTHORIZATION", token);
        return ResponseEntity.status(HttpStatus.CREATED).headers(headers)
                .body(userAccountDtoConverter.transform(userAccount));
    }


    @PostMapping("/changePassword")
    public ResponseEntity<Void> changePassword(@RequestBody @NotNull ChangePasswordDto changePasswordDto) {
        credentialsService.changePassword(changePasswordDto.getOldPassword(), changePasswordDto.getNewPassword());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<HttpHeaders> handleException(RuntimeException ex, HttpServletRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("messageError", "Something wrong: " + ex.getMessage()
                + "; path: " + request.getServletPath());
        return ResponseEntity.badRequest().headers(httpHeaders).build();
    }
}
