package com.psycorp.controller.api;

import com.psycorp.service.UserAnswersService;
import com.psycorp.service.UserMatchService;
import com.psycorp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class ApiTestController {
    private final UserAnswersService userAnswersService;
    private final UserService userService;
    private final UserMatchService userMatchService;
    private HttpHeaders httpHeaders;

    @Autowired
    public ApiTestController(UserAnswersService userAnswersService, UserService userService,
                             UserMatchService userMatchService) {
        this.userAnswersService = userAnswersService;
        this.userService = userService;
        this.userMatchService = userMatchService;
        httpHeaders = new HttpHeaders();
        httpHeaders.add("success", "true");
    }




}
