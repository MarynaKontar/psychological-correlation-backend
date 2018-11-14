package com.psycorp.controller.api;

import com.psycorp.model.dto.SimpleUserDto;
import com.psycorp.model.dto.UserAnswersDto;
import com.psycorp.model.dto.ValueProfileDto;
import com.psycorp.model.entity.Choice;
import com.psycorp.model.entity.User;
import com.psycorp.model.entity.UserAnswers;
import com.psycorp.model.entity.ValueProfile;
import com.psycorp.model.enums.Area;
import com.psycorp.model.enums.UserRole;
import com.psycorp.service.UserAnswersService;
import com.psycorp.service.UserService;
import com.psycorp.service.security.AuthService;
import com.psycorp.service.security.TokenService;
import com.psycorp.сonverter.ChoiceDtoConverter;
import com.psycorp.сonverter.UserAnswersDtoConverter;
import com.psycorp.сonverter.UserDtoConverter;
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
public class ApiUserAnswersController {

    private final UserAnswersService userAnswersService;
    private final UserService userService;
    private final ChoiceDtoConverter choiceDtoConverter;
    private final UserDtoConverter userDtoConverter;
    private final UserAnswersDtoConverter userAnswersDtoConverter;
    private final AuthService authService;
    private final TokenService tokenService;
    private HttpHeaders httpHeaders;


    @Autowired
    public ApiUserAnswersController(UserAnswersService userAnswersService, UserService userService,
                                    ChoiceDtoConverter choiceDtoConverter, UserDtoConverter userDtoConverter,
                                    UserAnswersDtoConverter userAnswersDtoConverter,
                                    AuthService authService, TokenService tokenService) {
        this.userAnswersService = userAnswersService;
        this.userService = userService;
        this.choiceDtoConverter = choiceDtoConverter;
        this.userDtoConverter = userDtoConverter;
        this.userAnswersDtoConverter = userAnswersDtoConverter;
        this.authService = authService;
        this.tokenService = tokenService;
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
    //TODO сделать, чтобы возврашал Void (чтобі лишняя инфа и нагрузка не ходила по http), потом как отдельній запрос
    // идет UserAnswersDto ("дайте мне результаты моего теста"). токен генерить в сервисе или как здесь?
    @PostMapping("/goal")
    public ResponseEntity<UserAnswersDto> saveGoal(@RequestBody @NotNull @Valid UserAnswersDto userAnswersDto
            , @RequestHeader(value = "Authorization", required = false) String token){
        UserAnswers userAnswers = userAnswersDtoConverter.transform(userAnswersDto);
        List<Choice> choices = choiceDtoConverter.transform(userAnswersDto.getGoal());
        userAnswers = userAnswersService.saveChoices(token, userAnswers, choices, Area.GOAL);
        if((token == null) && userAnswers.getUser().getRole().equals(UserRole.ANONIM)){
            token = ACCESS_TOKEN_PREFIX + " " + authService.generateAccessTokenForAnonim(userAnswers.getUser());
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.AUTHORIZATION, token) // или сгенеренный токен или пришедший в хедере (уже с 'Bearer ')
                .headers(httpHeaders).body(userAnswersDtoConverter.transform(userAnswers));
//        return ResponseEntity.ok().headers(httpHeaders).body(userAnswersDtoConverter.transform(userAnswers));
    }

    //save only qualities
    @PostMapping("/quality")
    public ResponseEntity<UserAnswersDto> saveQuality(@RequestBody @NotNull @Valid UserAnswersDto userAnswersDto) {
        UserAnswers userAnswers = userAnswersDtoConverter.transform(userAnswersDto);
        List<Choice> choices = choiceDtoConverter.transform(userAnswersDto.getQuality());
        userAnswers = userAnswersService.saveChoices(null, userAnswers, choices, Area.QUALITY);
        return ResponseEntity.ok().headers(httpHeaders).body(userAnswersDtoConverter.transform(userAnswers));
    }

    //save only states
    @PostMapping("/state")
    public ResponseEntity<UserAnswersDto> saveState(@RequestBody @NotNull @Valid UserAnswersDto userAnswersDto) {
        UserAnswers userAnswers = userAnswersDtoConverter.transform(userAnswersDto);
        List<Choice> choices = choiceDtoConverter.transform(userAnswersDto.getState());
        userAnswers = userAnswersService.saveChoices(null, userAnswers, choices, Area.STATE);
        return ResponseEntity.ok().headers(httpHeaders).body(userAnswersDtoConverter.transform(userAnswers));
    }

    @GetMapping("/getPassedTest")
    public ResponseEntity<UserAnswersDto> getPassedTestResult(){
         return ResponseEntity.ok().headers(httpHeaders)
                 .body(userAnswersDtoConverter.transform(userAnswersService.getLastPassedTest()));
    }

    @GetMapping("/generateTokenList")
    public ResponseEntity<List<String>> generateTokenList(){
        return ResponseEntity.ok().headers(httpHeaders).body(tokenService.generateTokenList(3));
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
                .transform(userAnswersService.findLastUserAnswersByUserNameOrEmail(userName)));
    }

    @GetMapping
    public ResponseEntity<List<UserAnswersDto>> getUserAnswers(@RequestParam @NotEmpty String userName){

        return ResponseEntity.ok().headers(httpHeaders).body(userAnswersDtoConverter.transform(
                userAnswersService.findAllByUserNameOrderByCreationDateDesc(userName)));

    }

    @PostMapping("/value-profile")
    public ResponseEntity<ValueProfileDto> getValueProfile(@RequestBody(required = false)
                                                           @NotNull @Valid SimpleUserDto userDto){

        User user = (userDto != null) ? userDtoConverter.transform(userDto) : null;
        ValueProfile valueProfile = userAnswersService.getValueProfile(user);
        ValueProfileDto valueProfileDtos = userAnswersDtoConverter.convertToValueProfileDtoList(valueProfile);
        return ResponseEntity.ok().headers(httpHeaders).body(valueProfileDtos);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<HttpHeaders> handleException(RuntimeException ex, HttpServletRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("messageError", "Something wrong: " + ex.getMessage()
                + "; path: " + request.getServletPath());
        return ResponseEntity.badRequest().headers(httpHeaders).build();
    }
}
