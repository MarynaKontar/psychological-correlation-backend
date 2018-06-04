package com.psycorp.controller.api;

import com.psycorp.exception.BadRequestException;
import com.psycorp.model.dto.ChoiceDto;
import com.psycorp.model.dto.UserAnswersDto;
import com.psycorp.model.dto.UserMatchDto;
import com.psycorp.model.entity.User;
import com.psycorp.model.entity.UserAnswers;
import com.psycorp.model.enums.MatchMethod;
import com.psycorp.service.UserAnswersService;
import com.psycorp.service.UserMatchService;
import com.psycorp.service.UserService;
import com.psycorp.util.Entity;
import com.psycorp.сonverter.ChoiceDtoConverter;
import com.psycorp.сonverter.UserAnswersDtoConverter;
import com.psycorp.сonverter.UserMatchDtoConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api")
public class ApiTestController {
    private final UserAnswersService userAnswersService;
    private final UserService userService;
    private final UserMatchService userMatchService;
    private final ChoiceDtoConverter choiceDtoConverter;
    private final UserAnswersDtoConverter userAnswersDtoConverter;
    private final UserMatchDtoConverter userMatchDtoConverter;
    private Principal principal;
    private HttpHeaders httpHeaders;

    @Autowired
    public ApiTestController(UserAnswersService userAnswersService, UserService userService,
                             UserMatchService userMatchService, ChoiceDtoConverter choiceDtoConverter,
                             UserAnswersDtoConverter userAnswersDtoConverter,
                             UserMatchDtoConverter userMatchDtoConverter) {
        this.userAnswersService = userAnswersService;
        this.userService = userService;
        this.userMatchService = userMatchService;
        this.choiceDtoConverter = choiceDtoConverter;
        this.userAnswersDtoConverter = userAnswersDtoConverter;
        this.userMatchDtoConverter = userMatchDtoConverter;

        httpHeaders = new HttpHeaders();
        httpHeaders.add("success", "true");
    }





    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<HttpHeaders> handleException(RuntimeException ex, HttpServletRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("success", "false");
        httpHeaders.add("messageError", "Something wrong: " + ex.getMessage());
        return ResponseEntity.badRequest().headers(httpHeaders).build();
    }


}
