package com.psycorp.controller.api;

import com.psycorp.service.ValueCompatibilityAnswersService;
import com.psycorp.service.UserMatchService;
import com.psycorp.service.UserService;
import com.psycorp.сonverter.ValueCompatibilityAnswersDtoConverter;
import com.psycorp.сonverter.UserMatchDtoConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

@RestController
@RequestMapping
public class ApiTestController {

    private final ValueCompatibilityAnswersService valueCompatibilityAnswersService;
    private final UserService userService;
    private final UserMatchService userMatchService;
    private final ValueCompatibilityAnswersDtoConverter valueCompatibilityAnswersDtoConverter;
    private final UserMatchDtoConverter userMatchDtoConverter;
    private Principal principal;
    private HttpHeaders httpHeaders;

    @Autowired
    public ApiTestController(ValueCompatibilityAnswersService valueCompatibilityAnswersService, UserService userService,
                             UserMatchService userMatchService, ValueCompatibilityAnswersDtoConverter valueCompatibilityAnswersDtoConverter,
                             UserMatchDtoConverter userMatchDtoConverter) {
        this.valueCompatibilityAnswersService = valueCompatibilityAnswersService;
        this.userService = userService;
        this.userMatchService = userMatchService;
        this.valueCompatibilityAnswersDtoConverter = valueCompatibilityAnswersDtoConverter;
        this.userMatchDtoConverter = userMatchDtoConverter;
    }


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<HttpHeaders> handleException(RuntimeException ex, HttpServletRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("messageError", "Something wrong: " + ex.getMessage());
        return ResponseEntity.badRequest().headers(httpHeaders).build();
    }


}
