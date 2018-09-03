package com.psycorp.controller.api;

import com.psycorp.service.UserAnswersService;
import com.psycorp.service.UserMatchService;
import com.psycorp.service.UserService;
import com.psycorp.сonverter.UserAnswersDtoConverter;
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
    @Autowired
    private UserAnswersService userAnswersService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserMatchService userMatchService;
    @Autowired
    private UserAnswersDtoConverter userAnswersDtoConverter;
    @Autowired
    private UserMatchDtoConverter userMatchDtoConverter;
    private Principal principal;
    private HttpHeaders httpHeaders;

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<HttpHeaders> handleException(RuntimeException ex, HttpServletRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("messageError", "Something wrong: " + ex.getMessage());
        return ResponseEntity.badRequest().headers(httpHeaders).build();
    }


}
