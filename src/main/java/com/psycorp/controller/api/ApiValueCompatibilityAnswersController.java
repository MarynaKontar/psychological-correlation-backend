package com.psycorp.controller.api;

import com.psycorp.model.dto.SimpleUserDto;
import com.psycorp.model.dto.ValueCompatibilityAnswersDto;
import com.psycorp.model.dto.ValueProfileIndividualDto;
import com.psycorp.model.entity.Choice;
import com.psycorp.model.entity.User;
import com.psycorp.model.entity.ValueCompatibilityAnswersEntity;
import com.psycorp.model.enums.Area;
import com.psycorp.model.enums.UserRole;
import com.psycorp.model.objects.ValueProfileIndividual;
import com.psycorp.service.ValueCompatibilityAnswersService;
import com.psycorp.service.ValueProfileService;
import com.psycorp.service.security.AuthService;
import com.psycorp.service.security.TokenService;
import com.psycorp.сonverter.*;
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
import java.util.List;

import static com.psycorp.security.SecurityConstant.ACCESS_TOKEN_PREFIX;

@RestController
@RequestMapping("/test")
@PropertySource("classpath:errormessages.properties")
@PropertySource(value = {"classpath:testing/scalesquestionsukrainian.properties"}, encoding = "utf-8", ignoreResourceNotFound = true)
@PropertySource(value = {"classpath:testing/scalesquestionsrussian.properties"}, encoding = "utf-8")
@PropertySource(value = {"classpath:testing/scalesquestionsenglish.properties"}, encoding = "utf-8", ignoreResourceNotFound = true)
@PropertySource(value = {"classpath:valueprofile/scalescommentrussian.properties"}, encoding = "utf-8")
//@PropertySource(value = {"classpath:common.properties"}, encoding = "utf-8")
public class ApiValueCompatibilityAnswersController {

    private final ValueCompatibilityAnswersService valueCompatibilityAnswersService;
    private final ValueProfileService valueProfileService;
    private final ValueProfileIndividualDtoConverter valueProfileIndividualDtoConverter;
    private final ChoiceDtoConverter choiceDtoConverter;
    private final UserDtoConverter userDtoConverter;
    private final ValueCompatibilityAnswersDtoConverter valueCompatibilityAnswersDtoConverter;
    private final AuthService authService;
    private final TokenService tokenService;
    private HttpHeaders httpHeaders;


    @Autowired
    public ApiValueCompatibilityAnswersController(ValueCompatibilityAnswersService valueCompatibilityAnswersService,
                                                  ValueProfileService valueProfileService,
                                                  ValueProfileIndividualDtoConverter valueProfileIndividualDtoConverter,
                                                  ChoiceDtoConverter choiceDtoConverter, UserDtoConverter userDtoConverter,
                                                  ValueCompatibilityAnswersDtoConverter valueCompatibilityAnswersDtoConverter,
                                                  AuthService authService, TokenService tokenService) {
        this.valueCompatibilityAnswersService = valueCompatibilityAnswersService;
        this.valueProfileService = valueProfileService;
        this.valueProfileIndividualDtoConverter = valueProfileIndividualDtoConverter;
        this.choiceDtoConverter = choiceDtoConverter;
        this.userDtoConverter = userDtoConverter;
        this.valueCompatibilityAnswersDtoConverter = valueCompatibilityAnswersDtoConverter;
        this.authService = authService;
        this.tokenService = tokenService;
        this.httpHeaders = new HttpHeaders();
    }

    @GetMapping("/initTest")
    public ResponseEntity<ValueCompatibilityAnswersDto> getInitValueCompatibilityAnswers(){
        return ResponseEntity.ok().headers(httpHeaders).body(valueCompatibilityAnswersDtoConverter
                .transform(valueCompatibilityAnswersService.getInitValueCompatibilityAnswers()));
    }

//    @PostMapping
//    public ResponseEntity<ValueCompatibilityAnswersDto> save(@RequestBody @NotNull @Valid ValueCompatibilityAnswersDto valueCompatibilityAnswersDto){
//
//        ValueCompatibilityAnswersEntity valueCompatibilityAnswers = valueCompatibilityAnswersDtoConverter.transform(valueCompatibilityAnswersDto);
//        valueCompatibilityAnswers = valueCompatibilityAnswersService.save(valueCompatibilityAnswers);
//        valueCompatibilityAnswersDto = valueCompatibilityAnswersDtoConverter.transform(valueCompatibilityAnswers);
//        return new ResponseEntity<>(valueCompatibilityAnswersDto, HttpStatus.CREATED);
//    }

    //save only goals
    //TODO сделать, чтобы возврашал Void (чтобі лишняя инфа и нагрузка не ходила по http), потом как отдельній запрос
    // идет ValueCompatibilityAnswersDto ("дайте мне результаты моего теста"). токен генерить в сервисе или как здесь?
    @PostMapping("/goal")
    public ResponseEntity<ValueCompatibilityAnswersDto> saveGoal(@RequestBody @NotNull @Valid ValueCompatibilityAnswersDto valueCompatibilityAnswersDto
            , @RequestHeader(value = "Authorization", required = false) String token){
        ValueCompatibilityAnswersEntity valueCompatibilityAnswersEntity = valueCompatibilityAnswersDtoConverter.transform(valueCompatibilityAnswersDto);
        List<Choice> choices = choiceDtoConverter.transform(valueCompatibilityAnswersDto.getGoal());
        valueCompatibilityAnswersEntity = valueCompatibilityAnswersService.saveChoices(token, valueCompatibilityAnswersEntity, choices, Area.GOAL);
        if((token == null) && valueCompatibilityAnswersEntity.getUser().getRole().equals(UserRole.ANONIM)){
            token = ACCESS_TOKEN_PREFIX + " " + authService.generateAccessTokenForAnonim(valueCompatibilityAnswersEntity.getUser());
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.AUTHORIZATION, token) // или сгенеренный токен или пришедший в хедере (уже с 'Bearer ')
                .headers(httpHeaders).body(valueCompatibilityAnswersDtoConverter.transform(valueCompatibilityAnswersEntity));
//        return ResponseEntity.ok().headers(httpHeaders).body(valueCompatibilityAnswersDtoConverter.transform(valueCompatibilityAnswersEntity));
    }

    //save only qualities
    @PostMapping("/quality")
    public ResponseEntity<ValueCompatibilityAnswersDto> saveQuality(@RequestBody @NotNull @Valid ValueCompatibilityAnswersDto valueCompatibilityAnswersDto) {
        ValueCompatibilityAnswersEntity valueCompatibilityAnswersEntity = valueCompatibilityAnswersDtoConverter.transform(valueCompatibilityAnswersDto);
        List<Choice> choices = choiceDtoConverter.transform(valueCompatibilityAnswersDto.getQuality());
        valueCompatibilityAnswersEntity = valueCompatibilityAnswersService.saveChoices(null, valueCompatibilityAnswersEntity, choices, Area.QUALITY);
        return ResponseEntity.ok().headers(httpHeaders).body(valueCompatibilityAnswersDtoConverter.transform(valueCompatibilityAnswersEntity));
    }

    //save only states
    @PostMapping("/state")
    public ResponseEntity<ValueCompatibilityAnswersDto> saveState(@RequestBody @NotNull @Valid ValueCompatibilityAnswersDto valueCompatibilityAnswersDto) {
        ValueCompatibilityAnswersEntity valueCompatibilityAnswersEntity = valueCompatibilityAnswersDtoConverter.transform(valueCompatibilityAnswersDto);
        List<Choice> choices = choiceDtoConverter.transform(valueCompatibilityAnswersDto.getState());
        valueCompatibilityAnswersEntity = valueCompatibilityAnswersService.saveChoices(null, valueCompatibilityAnswersEntity, choices, Area.STATE);
        return ResponseEntity.ok().headers(httpHeaders).body(valueCompatibilityAnswersDtoConverter.transform(valueCompatibilityAnswersEntity));
    }

    @GetMapping("/getPassedTest")
    public ResponseEntity<ValueCompatibilityAnswersDto> getPassedTestResult(){
         return ResponseEntity.ok().headers(httpHeaders)
                 .body(valueCompatibilityAnswersDtoConverter.transform(valueCompatibilityAnswersService.getLastPassedTest()));
    }

    @GetMapping("/generateTokenList")
    public ResponseEntity<List<String>> generateInviteTokenList(){
        return ResponseEntity.ok().headers(httpHeaders).body(tokenService.generateInviteTokenList(3));
    }


//    // TODO убрать в production
//    @PostMapping("/random/{userName}")
//    public ResponseEntity<ValueCompatibilityAnswersDto> testRandom(@PathVariable String userName, Principal principal){
//
//        User user = userService.findFirstUserByName(userName);
//        ValueCompatibilityAnswersEntity valueCompatibilityAnswers = Entity.createRandomUserAnswers(user);
//        valueCompatibilityAnswers = valueCompatibilityAnswersService.save(valueCompatibilityAnswers);
//        ValueCompatibilityAnswersDto valueCompatibilityAnswersDto = valueCompatibilityAnswersDtoConverter.transform(valueCompatibilityAnswers);
//        valueCompatibilityAnswersDto.setPassed(true);
//        return new ResponseEntity<>(valueCompatibilityAnswersDto, HttpStatus.CREATED);
//    }

    @GetMapping("/{id}")
    public ResponseEntity<ValueCompatibilityAnswersDto> getLastTest(@PathVariable @NotNull ObjectId id){
        return ResponseEntity.ok().headers(httpHeaders).body(valueCompatibilityAnswersDtoConverter
                .transform(valueCompatibilityAnswersService.findById(id)));
    }
    @GetMapping("/userName")
    public ResponseEntity<ValueCompatibilityAnswersDto> getLastTest(@RequestParam @NotEmpty String userName){
        return ResponseEntity.ok().headers(httpHeaders).body(valueCompatibilityAnswersDtoConverter
                .transform(valueCompatibilityAnswersService.findLastValueCompatibilityAnswersByUserNameOrEmail(userName)));
    }

    @GetMapping
    public ResponseEntity<List<ValueCompatibilityAnswersDto>> getUserAnswers(@RequestParam @NotEmpty String userName){

        return ResponseEntity.ok().headers(httpHeaders).body(valueCompatibilityAnswersDtoConverter.transform(
                valueCompatibilityAnswersService.findAllByUserNameOrderByCreationDateDesc(userName)));

    }

    @PostMapping("/value-profile")
    public ResponseEntity<ValueProfileIndividualDto> getValueProfile(@RequestBody(required = false)
                                                           @NotNull @Valid SimpleUserDto userDto){

        User user = (userDto != null) ? userDtoConverter.transform(userDto) : null;
        ValueProfileIndividual valueProfile = valueProfileService.getValueProfileIndividual(user);
        ValueProfileIndividualDto valueProfileIndividualDto = valueProfileIndividualDtoConverter.transform(valueProfile);
        return ResponseEntity.ok().headers(httpHeaders).body(valueProfileIndividualDto);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<HttpHeaders> handleException(RuntimeException ex, HttpServletRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("messageError", "Something wrong: " + ex.getMessage()
                + "; path: " + request.getServletPath());
        return ResponseEntity.badRequest().headers(httpHeaders).build();
    }
}
