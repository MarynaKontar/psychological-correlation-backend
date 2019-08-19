package com.psycorp.controller.api;

import com.psycorp.exception.BadRequestException;
import com.psycorp.model.dto.SimpleUserDto;
import com.psycorp.model.entity.User;
import com.psycorp.service.UserService;
import com.psycorp.сonverter.UserDtoConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Rest controller for user.
 * url : "/user"
 * @author Maryna Kontar
 */
@RestController
@RequestMapping("/user")
@PropertySource("classpath:errormessages.properties")
public class ApiUserController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiUserController.class);

    private final UserService userService;
    private final UserDtoConverter userDtoConverter;

    @Autowired
    public ApiUserController(UserService userService,
                             UserDtoConverter userDtoConverter) {
        this.userService = userService;
        this.userDtoConverter = userDtoConverter;
    }

    /**
     * Endpoint for url ":/incompleteRegistration".
     * Incomplete user registration without password.
     * Available only for anonim registered (have token) or registered users.
     * Notice, that it`s not full registration, it`s only save name, age and gender of user to db.
     * An account is not created and the stored information does not contain a password,
     * the user role remains anonymous, if user isn't registered.
     * If in userDto will be an email, it will not be saved.
     * @param userDto dto that contain incomplete user information (name, age and gender).
     * @return ResponseEntity<UserAccountDto> with HttpStatus Ok and an incomplete registered user
     * in response body, if registration is success.
     * @throws BadRequestException if registration is failed.
     */
    @PostMapping("/incompleteRegistration")
    public ResponseEntity<SimpleUserDto> incompleteRegistration(@RequestBody @NotNull @Valid SimpleUserDto userDto) {
        LOGGER.trace("incompleteRegistration: {}", userDto);
        User user = userService.addNameAgeAndGender(userDtoConverter.transform(userDto));
        return ResponseEntity.ok().body(userDtoConverter.transform(user));
    }

    /**
     * RuntimeException handler.
     * @param ex RuntimeException for handling.
     * @param request HttpServletRequest which caused the RuntimeException.
     * @return ResponseEntity with HttpStatus BAD_REQUEST and exception message in header.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<HttpHeaders> handleException(RuntimeException ex, HttpServletRequest request) {
        LOGGER.trace("IP: {}:{}:{} : EXCEPTION: {}", request.getRemoteHost(), request.getRemotePort(), request.getRemoteUser(), ex.getMessage());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("messageError", ex.getMessage());
        httpHeaders.add("path", request.getServletPath());
        return new ResponseEntity<>(httpHeaders, httpHeaders, HttpStatus.BAD_REQUEST);
    }
}
