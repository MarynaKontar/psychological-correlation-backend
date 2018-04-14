package com.psycorp.controller.api;


import com.psycorp.model.entity.User;
import com.psycorp.model.entity.UserAnswers;
import com.psycorp.service.UserAnswersService;
import com.psycorp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/user")
public class ApiTestController {
    private final UserAnswersService userAnswersService;
    private final UserService userService;
    private HttpHeaders httpHeaders;

    @Autowired
    public ApiTestController(UserAnswersService userAnswersService, UserService userService) {
        this.userAnswersService = userAnswersService;
        this.userService = userService;
        httpHeaders = new HttpHeaders();
        httpHeaders.add("success", "true");
    }


    @PostMapping("/add")
    public ResponseEntity<User> save(@RequestBody User user) {
        return ResponseEntity.ok().headers(httpHeaders).body(userService.insert(user));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<HttpHeaders> handleException(RuntimeException ex, HttpServletRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("success", "false");
        httpHeaders.add("messageError", "Something wrong: " + ex.getMessage());
        return ResponseEntity.badRequest().headers(httpHeaders).build();
    }
}
