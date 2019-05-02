package com.psycorp.controller.api;

import com.psycorp.model.dto.CredentialsDto;
import com.psycorp.model.dto.SimpleUserDto;
import com.psycorp.model.entity.User;
import com.psycorp.service.CredentialsService;
import com.psycorp.сonverter.CredentialsEntityConverter;
import com.psycorp.сonverter.UserDtoConverter;
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
    private final UserDtoConverter userDtoConverter;
    private final CredentialsEntityConverter credentialsEntityConverter;

    public ApiRegistrationController(CredentialsService credentialsService, UserDtoConverter userDtoConverter, CredentialsEntityConverter credentialsEntityConverter) {
        this.credentialsService = credentialsService;
        this.userDtoConverter = userDtoConverter;
        this.credentialsEntityConverter = credentialsEntityConverter;
    }

    @PostMapping
    public ResponseEntity<SimpleUserDto> register(@RequestBody @NotNull @Valid CredentialsDto credentialsDto,
                                                  @RequestHeader(value = "Authorization", required = false) String token) {

        User user = credentialsService.save(credentialsEntityConverter.transform(credentialsDto), token);
        return new ResponseEntity<>(userDtoConverter.transform(user), HttpStatus.CREATED);
    }

    @PostMapping("/changePassword")
    public ResponseEntity<Void> changePassword(@RequestBody @NotNull CredentialsDto credentialsDto) {
        credentialsService.changePassword(credentialsEntityConverter.transform(credentialsDto));
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
