package com.psycorp.controller.api;

import com.psycorp.model.dto.ChoiceDto;
import com.psycorp.model.dto.UserAnswersDto;
import com.psycorp.model.entity.Choice;
import com.psycorp.model.entity.User;
import com.psycorp.model.entity.UserAnswers;
import com.psycorp.model.enums.Area;
import com.psycorp.service.UserAnswersService;
import com.psycorp.service.UserService;
import com.psycorp.util.Entity;
import com.psycorp.сonverter.ChoiceDtoConverter;
import com.psycorp.сonverter.UserAnswersDtoConverter;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/test")
@PropertySource("classpath:errormessages.properties")
public class ApiUserAnswersController {

    @Autowired
    private UserAnswersService userAnswersService;
    @Autowired
    private UserService userService;
    @Autowired
    private ChoiceDtoConverter choiceDtoConverter;
    @Autowired
    private UserAnswersDtoConverter userAnswersDtoConverter;
    private HttpHeaders httpHeaders;

    @GetMapping(value = "/initTest", produces = "application/json")
    public ResponseEntity<UserAnswersDto> getInitUserAnswers(){
        return ResponseEntity.ok().headers(httpHeaders).body(userAnswersDtoConverter.transform(userAnswersService.getInitUserAnswers()));
    }

    @PostMapping()
    public ResponseEntity<UserAnswersDto> save(@RequestBody @NotNull @Valid UserAnswersDto userAnswersDto){

        UserAnswers userAnswers = userAnswersDtoConverter.transform(userAnswersDto);
        userAnswers = userAnswersService.save(userAnswers);
        userAnswersDto = userAnswersDtoConverter.transform(userAnswers);
        return new ResponseEntity<>(userAnswersDto, HttpStatus.CREATED);
    }

    //save only goals
    @PostMapping(value= "/goal/{userName}")
    public ResponseEntity<UserAnswersDto> saveGoal(@PathVariable String userName
            , @RequestBody @NotNull @Valid List<ChoiceDto> choicesDto, Principal principal){
        UserAnswersDto userAnswersDto = saveChoices(choicesDto, Area.GOAL, principal, userName);
        return ResponseEntity.ok().headers(httpHeaders).body(userAnswersDto);
    }

    //save only qualities
    @PostMapping(value= "/quality/{userName}")
    public ResponseEntity<UserAnswersDto> saveQuality(@PathVariable String userName
            , @RequestBody @NotNull @Valid List<ChoiceDto> choicesDto, Principal principal) {
        UserAnswersDto userAnswersDto = saveChoices(choicesDto, Area.QUALITY, principal, userName);
        return ResponseEntity.ok().headers(httpHeaders).body(userAnswersDto);
    }

    //save only states
    @PostMapping(value= "/state/{userName}")
    public ResponseEntity<UserAnswersDto> saveState(@PathVariable String userName
            , @RequestBody @NotNull @Valid List<ChoiceDto> choicesDto, Principal principal) {
        UserAnswersDto userAnswersDto = saveChoices(choicesDto, Area.STATE, principal, userName);
        userAnswersDto.setPassed(true);
        return ResponseEntity.ok().headers(httpHeaders).body(userAnswersDto);
    }

    private UserAnswersDto saveChoices(List<ChoiceDto> choicesDto, Area area, Principal principal, String userName){
        List<Choice> choices = choiceDtoConverter.transform(choicesDto);
        userAnswersService.validateArea(choices, area);
        UserAnswers userAnswers = userAnswersService.saveChoices(choices, principal, userName);
        return userAnswersDtoConverter.transform(userAnswers);

    }

    @PostMapping(value= "/random/{userName}")
    public ResponseEntity<UserAnswersDto> testRandom(@PathVariable String userName, Principal principal){

        User user = userService.findFirstUserByName(userName);
        UserAnswers userAnswers = Entity.createRandomUserAnswers(user);
        userAnswers = userAnswersService.save(userAnswers);
        UserAnswersDto userAnswersDto = userAnswersDtoConverter.transform(userAnswers);
        userAnswersDto.setPassed(true);
        return new ResponseEntity<>(userAnswersDto, HttpStatus.CREATED);
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<UserAnswersDto> getLastTest(@PathVariable @NotNull ObjectId id){
        return ResponseEntity.ok().headers(httpHeaders).body(userAnswersDtoConverter
                .transform(userAnswersService.findById(id)));
    }
    @GetMapping(value = "/userName/{userName}", produces = "application/json")
    public ResponseEntity<UserAnswersDto> getLastTest(@PathVariable String userName){
        return ResponseEntity.ok().headers(httpHeaders).body(userAnswersDtoConverter
                .transform(userAnswersService.findLastUserAnswersByUserName(userName)));
    }

    @GetMapping(value = "/getAll/{userName}", produces = "application/json")
    public ResponseEntity<List<UserAnswersDto>> getUserAnswers(@PathVariable String userName){

        return ResponseEntity.ok().headers(httpHeaders).body(userAnswersDtoConverter.transform(
                userAnswersService.findAllByUserNameOrderByCreationDateDesc(userName)));

    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<HttpHeaders> handleException(RuntimeException ex, HttpServletRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("messageError", "Something wrong: " + ex.getMessage()
                + "; path: " + request.getServletPath());
        return ResponseEntity.badRequest().headers(httpHeaders).build();
    }

}
