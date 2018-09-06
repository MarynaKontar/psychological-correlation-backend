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
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/test")
@PropertySource("classpath:errormessages.properties")
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
        this.httpHeaders = new HttpHeaders();
    }

    @GetMapping("/initTest")
    public ResponseEntity<UserAnswersDto> getInitUserAnswers(){
        return ResponseEntity.ok().headers(httpHeaders).body(userAnswersDtoConverter.transform(userAnswersService.getInitUserAnswers()));
    }

//    @PostMapping
//    public ResponseEntity<UserAnswersDto> save(@RequestBody @NotNull @Valid UserAnswersDto userAnswersDto){
//
//        UserAnswers userAnswers = userAnswersDtoConverter.transform(userAnswersDto);
//        userAnswers = userAnswersService.save(userAnswers);
//        userAnswersDto = userAnswersDtoConverter.transform(userAnswers);
//        return new ResponseEntity<>(userAnswersDto, HttpStatus.CREATED);
//    }

    //save only goals
    @PostMapping("/goal/{userName}")
    public ResponseEntity<UserAnswersDto> saveGoal(@PathVariable String userName
            , @RequestBody @NotNull @Valid UserAnswersDto userAnswersDto, Principal principal){
        UserAnswers userAnswers = userAnswersDtoConverter.transform(userAnswersDto);
        List<Choice> choices = choiceDtoConverter.transform(userAnswersDto.getGoal());
        userAnswers = userAnswersService.saveChoices(userAnswers, choices, principal, Area.GOAL, userName);
        return ResponseEntity.ok().headers(httpHeaders).body(userAnswersDtoConverter.transform(userAnswers));
    }

    //save only qualities
    @PostMapping("/quality/{userName}")
    public ResponseEntity<UserAnswersDto> saveQuality(@PathVariable String userName
            , @RequestBody @NotNull @Valid UserAnswersDto userAnswersDto, Principal principal) {
        UserAnswers userAnswers = userAnswersDtoConverter.transform(userAnswersDto);
        List<Choice> choices = choiceDtoConverter.transform(userAnswersDto.getQuality());
        userAnswers = userAnswersService.saveChoices(userAnswers, choices, principal, Area.QUALITY, userName);
        return ResponseEntity.ok().headers(httpHeaders).body(userAnswersDtoConverter.transform(userAnswers));
    }

    //save only states
    @PostMapping("/state/{userName}")
    public ResponseEntity<UserAnswersDto> saveState(@PathVariable String userName
            , @RequestBody @NotNull @Valid UserAnswersDto userAnswersDto, Principal principal) {
        UserAnswers userAnswers = userAnswersDtoConverter.transform(userAnswersDto);
        List<Choice> choices = choiceDtoConverter.transform(userAnswersDto.getState());
        userAnswers = userAnswersService.saveChoices(userAnswers, choices, principal, Area.STATE, userName);
        return ResponseEntity.ok().headers(httpHeaders).body(userAnswersDtoConverter.transform(userAnswers));
    }

//    // TODO убрать в production
//    @PostMapping("/random/{userName}")
//    public ResponseEntity<UserAnswersDto> testRandom(@PathVariable String userName, Principal principal){
//
//        User user = userService.findFirstUserByName(userName);
//        UserAnswers userAnswers = Entity.createRandomUserAnswers(user);
//        userAnswers = userAnswersService.save(userAnswers);
//        UserAnswersDto userAnswersDto = userAnswersDtoConverter.transform(userAnswers);
//        userAnswersDto.setPassed(true);
//        return new ResponseEntity<>(userAnswersDto, HttpStatus.CREATED);
//    }

    @GetMapping("/{id}")
    public ResponseEntity<UserAnswersDto> getLastTest(@PathVariable @NotNull ObjectId id){
        return ResponseEntity.ok().headers(httpHeaders).body(userAnswersDtoConverter
                .transform(userAnswersService.findById(id)));
    }
    @GetMapping("/userName")
    public ResponseEntity<UserAnswersDto> getLastTest(@RequestParam @NotEmpty String userName){
        return ResponseEntity.ok().headers(httpHeaders).body(userAnswersDtoConverter
                .transform(userAnswersService.findLastUserAnswersByUserName(userName)));
    }

    @GetMapping
    public ResponseEntity<List<UserAnswersDto>> getUserAnswers(@RequestParam @NotEmpty String userName){

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
