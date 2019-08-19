package com.psycorp.controller.api;

import com.psycorp.model.dto.SomeDto;
import com.psycorp.service.ValueCompatibilityAnswersService;
import com.psycorp.service.UserMatchService;
import com.psycorp.service.UserService;
import com.psycorp.сonverter.UserDtoConverter;
import com.psycorp.сonverter.ValueCompatibilityAnswersDtoConverter;
import com.psycorp.сonverter.UserMatchDtoConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.List;

/**
 * DELETE!!!!!!
 * Rest controller for testing different functionality.
 * url : "/"
 * @author Maryna Kontar
 */
@RestController
@RequestMapping
public class ApiTestController {

    private final ValueCompatibilityAnswersService valueCompatibilityAnswersService;
    private final UserService userService;
    private final UserMatchService userMatchService;
    private final UserDtoConverter userDtoConverter;
    private final ValueCompatibilityAnswersDtoConverter valueCompatibilityAnswersDtoConverter;
    private final UserMatchDtoConverter userMatchDtoConverter;
    private Principal principal;
    private HttpHeaders httpHeaders;

    @Autowired
    public ApiTestController(ValueCompatibilityAnswersService valueCompatibilityAnswersService,
                             UserService userService,
                             UserMatchService userMatchService,
                             UserDtoConverter userDtoConverter,
                             ValueCompatibilityAnswersDtoConverter valueCompatibilityAnswersDtoConverter,
                             UserMatchDtoConverter userMatchDtoConverter) {
        this.valueCompatibilityAnswersService = valueCompatibilityAnswersService;
        this.userService = userService;
        this.userMatchService = userMatchService;
        this.userDtoConverter = userDtoConverter;
        this.valueCompatibilityAnswersDtoConverter = valueCompatibilityAnswersDtoConverter;
        this.userMatchDtoConverter = userMatchDtoConverter;
    }


//    @GetMapping("/getVCAnswersWithUserInfo")
//    public ResponseEntity<List<SomeDto>> getSuitableUsers() {
//       return ResponseEntity.ok(userService.getVCAnswersWithUserInfo());
//    }

    /**
     * RuntimeException handler.
     * @param ex RuntimeException for handling.
     * @param request HttpServletRequest which caused the RuntimeException.
     * @return ResponseEntity with HttpStatus BAD_REQUEST and exception message in header.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<HttpHeaders> handleException(RuntimeException ex, HttpServletRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("messageError", "Something wrong: " + ex.getMessage());
        return ResponseEntity.badRequest().headers(httpHeaders).build();
    }


}
