package com.psycorp.controller.api;


import com.psycorp.model.dto.ChoiceDto;
import com.psycorp.model.dto.UserAnswersDto;
import com.psycorp.model.entity.User;
import com.psycorp.model.entity.UserAnswers;
import com.psycorp.service.UserAnswersService;
import com.psycorp.service.UserService;
import com.psycorp.util.Entity;
import com.psycorp.сonverter.ChoiceDtoConverter;
import com.psycorp.сonverter.UserAnswersDtoConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/test")
public class ApiUserAnswersController {

    private final UserAnswersService userAnswersService;
    private final UserService userService;
    private final ChoiceDtoConverter choiceDtoConverter;
    private final UserAnswersDtoConverter userAnswersDtoConverter;
    private HttpHeaders httpHeaders;

    @Autowired
    public ApiUserAnswersController(UserAnswersService userAnswersService, UserService userService,
                             ChoiceDtoConverter choiceDtoConverter,
                             UserAnswersDtoConverter userAnswersDtoConverter) {
        this.userAnswersService = userAnswersService;
        this.userService = userService;
        this.choiceDtoConverter = choiceDtoConverter;
        this.userAnswersDtoConverter = userAnswersDtoConverter;

        httpHeaders = new HttpHeaders();
        httpHeaders.add("success", "true");
    }

//    @GetMapping(value = "/testlist", produces = "application/json")
//    public ResponseEntity<List<ChoiceDto>> testlist(){
//        //TODO возвращать UserAnswers
//        return ResponseEntity.ok().headers(httpHeaders).body(choiceDtoConverter.transform(userAnswersService.choiceList()));
//    }

    @GetMapping(value = "/testlist", produces = "application/json")
    public ResponseEntity<UserAnswersDto> testlist(){

        return ResponseEntity.ok().headers(httpHeaders).body(userAnswersDtoConverter.transform(userAnswersService.getInitUserAnswers()));
    }









    @PostMapping(value= "/{userName}")
    public ResponseEntity<UserAnswersDto> test(@PathVariable String userName
            , @RequestBody UserAnswersDto userAnswersDto, Principal principal
    ){
        //TODO Надо переделать, учитывая как передать пользователя в userAnswersDto (через Principal?). В transform добавить principal
        // Убрать @PathVariable
//        if(!Objects.equals(principal.getName(), userName)) throw new BadRequestException("error.badrequest");

        UserAnswers userAnswers = userAnswersDtoConverter.transform(userAnswersDto);
        userAnswers = userAnswersService.insert(userAnswers);
//        userAnswers = userAnswersService.insert(userAnswers, principal);


        userAnswersDto = userAnswersDtoConverter.transform(userAnswers);
        return ResponseEntity.ok().headers(httpHeaders).body(userAnswersDto);
    }

    @PostMapping(value= "random/{userName}")
    public ResponseEntity<UserAnswersDto> testRandom(@PathVariable String userName, Principal principal){

        User user = userService.findFirstUserByName(userName);
//        User user = userService.findFirstUserByName(principal.getName());

        UserAnswers userAnswers = Entity.createRandomUserAnswers(user);
        userAnswers = userAnswersService.insert(userAnswers);
//        userAnswers = userAnswersService.insert(userAnswers, principal);
        UserAnswersDto userAnswersDto = userAnswersDtoConverter.transform(userAnswers);
        return ResponseEntity.ok().headers(httpHeaders).body(userAnswersDto);
    }

    @GetMapping(value = "/answers/{userName}", produces = "application/json")
    public ResponseEntity<List<UserAnswersDto>> getUserAnswers(@PathVariable("userName") String userName){
        return ResponseEntity.ok().headers(httpHeaders).body(userAnswersDtoConverter.transform(
                userAnswersService.findAllByUserNameOrderByCreationDateDesc(userName)));

    }

    @GetMapping(value = "/{userName}", produces = "application/json")
    public ResponseEntity<UserAnswersDto> getLastTest(@PathVariable String userName){
        return ResponseEntity.ok().headers(httpHeaders).body(userAnswersDtoConverter
                .transform(userAnswersService.findLastByUserName(userName)));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<HttpHeaders> handleException(RuntimeException ex, HttpServletRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("success", "false");
        httpHeaders.add("messageError", "Something wrong: " + ex.getMessage());
        return ResponseEntity.badRequest().headers(httpHeaders).build();
    }

}
