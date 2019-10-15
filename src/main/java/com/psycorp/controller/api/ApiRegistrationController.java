package com.psycorp.controller.api;

import com.psycorp.model.dto.ChangePasswordDto;
import com.psycorp.model.dto.CredentialsDto;
import com.psycorp.model.dto.UserAccountDto;
import com.psycorp.model.objects.Credentials;
import com.psycorp.model.objects.UserAccount;
import com.psycorp.service.CredentialsService;
import com.psycorp.service.security.TokenService;
import com.psycorp.сonverter.CredentialsDtoConverter;
import com.psycorp.сonverter.UserAccountDtoConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Rest controller for user registration.
 * url : "/registration"
 * @author Maryna Kontar
 */
@RestController
@RequestMapping("/registration")
@PropertySource("classpath:errormessages.properties")
public class ApiRegistrationController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiRegistrationController.class);

    private final CredentialsService credentialsService;
    private final TokenService tokenService;
    private final UserAccountDtoConverter userAccountDtoConverter;
    private final CredentialsDtoConverter credentialsDtoConverter;

    public ApiRegistrationController(CredentialsService credentialsService,
                                     TokenService tokenService,
                                     UserAccountDtoConverter userAccountDtoConverter,
                                     CredentialsDtoConverter credentialsDtoConverter) {
        this.credentialsService = credentialsService;
        this.tokenService = tokenService;
        this.userAccountDtoConverter = userAccountDtoConverter;
        this.credentialsDtoConverter = credentialsDtoConverter;
    }

    /**
     * Endpoint for url ":/registration".
     * User registration.
     * Notice, that in addition to registration, token is taken and returned in headers.
     * @param credentialsDto dto that contain user information for registration including password.
     * @param token user token if registered user is anonim registered.
     * @return ResponseEntity<UserAccountDto> with HttpStatus CREATED and an registered
     * user account in response body and access token in header, if registration is success.
     */
    @PostMapping
    public ResponseEntity<UserAccountDto> register(@RequestBody @NotNull @Valid CredentialsDto credentialsDto,
                                                   @RequestHeader(value = "Authorization", required = false) String token) {

        LOGGER.trace("registration('{}','{}')", credentialsDto, token);
        UserAccount userAccount = credentialsService.save(credentialsDtoConverter.transform(credentialsDto));
        token = tokenService.getTokenForRegisteredUser(token, userAccount.getUser().getId());
        HttpHeaders headers = new HttpHeaders();
        headers.set("AUTHORIZATION", token);
        LOGGER.trace("Registration success: '{}'", userAccount.getUser().getId());
        UserAccountDto userAccountDto = userAccountDtoConverter.transform(userAccount);
        return ResponseEntity.status(HttpStatus.CREATED)
                .headers(headers)
                .body(userAccountDto);
    }

    /**
     * Endpoint for url ":/registration/changePassword".
     * Changes password.
     * @param changePasswordDto contain old and new password.
     * @return ResponseEntity<> with HttpStatus NO_CONTENT if the password was successfully changed.
     */
    @PostMapping("/changePassword")
    public ResponseEntity<Void> changePassword(@RequestBody @NotNull ChangePasswordDto changePasswordDto) {
        LOGGER.trace("changePassword");
        credentialsService.changePassword(changePasswordDto.getOldPassword(), changePasswordDto.getNewPassword());
        LOGGER.trace("Change password success");
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
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
        httpHeaders.add("messageError", "Something wrong: " + ex.getMessage()
                        + "; path: " + request.getServletPath());
        return ResponseEntity.badRequest().headers(httpHeaders).build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<HttpHeaders> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        LOGGER.trace("IP: {}:{}:{} : MethodArgumentNotValidException: {}", request.getRemoteHost(), request.getRemotePort(), request.getRemoteUser(), ex);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("messageError", "Something wrong: " + ex.getMessage()
                + "; path: " + request.getServletPath());
        return ResponseEntity.badRequest().headers(httpHeaders).build();
    }
}
