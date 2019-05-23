package com.psycorp.controller.api;

import com.psycorp.model.dto.CredentialsDto;
import com.psycorp.model.dto.UserAccountDto;
import com.psycorp.model.entity.User;
import com.psycorp.model.objects.UserAccount;
import com.psycorp.service.CredentialsService;
import com.psycorp.service.UserAccountService;
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

@RestController
@RequestMapping("/registration")
@PropertySource("classpath:errormessages.properties")
public class ApiRegistrationController {

    private final CredentialsService credentialsService;
    private final UserAccountDtoConverter userAccountDtoConverter;
    private final CredentialsDtoConverter credentialsDtoConverter;

    public ApiRegistrationController(CredentialsService credentialsService,
                                     UserAccountDtoConverter userAccountDtoConverter,
                                     CredentialsDtoConverter credentialsDtoConverter) {
        this.credentialsService = credentialsService;
        this.userAccountDtoConverter = userAccountDtoConverter;
        this.credentialsDtoConverter = credentialsDtoConverter;
    }

    @PostMapping
    public ResponseEntity<UserAccountDto> register(@RequestBody @NotNull @Valid CredentialsDto credentialsDto,
                                                   @RequestHeader(value = "Authorization", required = false) String token) {

        UserAccount userAccount = credentialsService.save(credentialsDtoConverter.transform(credentialsDto), token);
        return new ResponseEntity<>(userAccountDtoConverter.transform(userAccount), HttpStatus.CREATED);
    }

    @PostMapping("/changePassword")
    public ResponseEntity<Void> changePassword(@RequestBody @NotNull CredentialsDto credentialsDto) {
        credentialsService.changePassword(credentialsDtoConverter.transform(credentialsDto));
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
