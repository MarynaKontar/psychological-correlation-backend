package com.psycorp.controller.api;

import com.psycorp.exception.BadRequestException;
import com.psycorp.model.dto.SimpleUserDto;
import com.psycorp.model.dto.UserMatchDto;
import com.psycorp.model.dto.ValueProfileDto;
import com.psycorp.model.entity.User;
import com.psycorp.model.entity.ValueProfile;
import com.psycorp.model.enums.MatchMethod;
import com.psycorp.service.UserAnswersService;
import com.psycorp.service.UserMatchService;
import com.psycorp.service.UserService;
import com.psycorp.сonverter.UserAnswersDtoConverter;
import com.psycorp.сonverter.UserDtoConverter;
import com.psycorp.сonverter.UserMatchDtoConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/match")
@PropertySource("classpath:errormessages.properties")
public class ApiUserMatchController {

    private final UserService userService;
    private final UserMatchService userMatchService;
    private final UserDtoConverter userDtoConverter;
    private final UserAnswersService userAnswersService;
    private final UserAnswersDtoConverter userAnswersDtoConverter;
    private final UserMatchDtoConverter userMatchDtoConverter;
    private HttpHeaders httpHeaders;

    @Autowired
    public ApiUserMatchController(UserService userService, UserMatchService userMatchService,
                                  UserDtoConverter userDtoConverter, UserAnswersService userAnswersService,
                                  UserAnswersDtoConverter userAnswersDtoConverter,
                                  UserMatchDtoConverter userMatchDtoConverter) {
        this.userService = userService;
        this.userMatchService = userMatchService;
        this.userDtoConverter = userDtoConverter;
        this.userAnswersService = userAnswersService;
        this.userAnswersDtoConverter = userAnswersDtoConverter;
        this.userMatchDtoConverter = userMatchDtoConverter;
        this.httpHeaders = new HttpHeaders();
    }

    @GetMapping("/getUsersForMatching")
    public ResponseEntity<List<User>> getUsersForMatching() {
        return ResponseEntity.ok().headers(httpHeaders)
                .body(userService.getPrincipalUser().getUsersForMatching());
    }

    // TODO если будет сравнивать больше двух пользователей, то List<SimpleUserDto> userDtos с фронта
    @PostMapping("/value-profile-for-matching")
    public ResponseEntity<List<ValueProfileDto>> getValueProfilesForMatching(@RequestBody
                                         @NotNull @Valid SimpleUserDto userDto) throws BadRequestException {
       User user;
       if(userDto != null) {
           user = userDtoConverter.transform(userDto);
       } else {throw new BadRequestException("User can't be null.");}

       ValueProfile valueProfilePrincipal = userAnswersService.getValueProfile(null);
       ValueProfile valueProfile = userAnswersService.getValueProfile(user);
       ValueProfileDto valueProfilePrincipalDto = userAnswersDtoConverter.convertToValueProfileDtoList(valueProfilePrincipal);
       ValueProfileDto valueProfileDto = userAnswersDtoConverter.convertToValueProfileDtoList(valueProfile);
       List<ValueProfileDto> valueProfileDtos =Arrays.asList(valueProfileDto, valueProfilePrincipalDto);
       return ResponseEntity.ok().headers(httpHeaders).body(valueProfileDtos);
    }


    //TODO надо передавать лист с users для тех, с кем идет сравнение
    @PostMapping(value = "/Pearson")
    public ResponseEntity<UserMatchDto> getUserMatchPearson(@RequestBody(required = false) SimpleUserDto userDto){

        User user = (userDto != null) ? userDtoConverter.transform(userDto) : null;
        return ResponseEntity.created(httpHeaders.getLocation())
                .headers(httpHeaders).body(userMatchDtoConverter.transform(userMatchService
                        .match(user, MatchMethod.PEARSONCORRELATION)));
    }


//    @PostMapping(value = "/Pearson")
//    public ResponseEntity<UserMatchDto> getUserMatchPearson(@RequestBody String token){
//        return ResponseEntity.created(httpHeaders.getLocation())
//                .headers(httpHeaders).body(userMatchDtoConverter.transform(userMatchService
//                        .match(token, MatchMethod.PEARSONCORRELATION)));
//    }


//    @PostMapping(value = "/Pearson/{userName1}/{userName2}", produces = "application/json")
//    public ResponseEntity<UserMatchDto> getUserMatchPearson(@PathVariable("userName1") String userName1
//            , @PathVariable("userName2") String userName2, Principal principal){
//
////        if(principal.getName() == null ||
////                (!principal.getName().equals(userName1) && !principal.getName().equals(userName2)) )
////            return ResponseEntity.badRequest().build();
//        return ResponseEntity.created(httpHeaders.getLocation())
//                .headers(httpHeaders).body(userMatchDtoConverter.transform(userMatchService
//                .match(userService.findFirstUserByName(userName1), userService.findFirstUserByName(userName2)
//                        , MatchMethod.PEARSONCORRELATION)));
//    }

    @PostMapping(value = "/Percent")
    public ResponseEntity<UserMatchDto> getUserMatchPercent(@RequestBody(required = false) SimpleUserDto userDto){

        User user = (userDto != null) ? userDtoConverter.transform(userDto) : null;
        return ResponseEntity.created(httpHeaders.getLocation())
                .headers(httpHeaders).body(userMatchDtoConverter.transform(userMatchService
                        .match(user, MatchMethod.PERCENT)));
    }

    @GetMapping(value = "/{userName}", produces = "application/json")
    public ResponseEntity<List<UserMatchDto>> getAllMatches(Principal principal, @PathVariable String userName){
//TODO после подключения security, убрать @PathVariable String userName и использовать principal.getName()
//        User user = userService.findFirstUserByName(userName);
//        if (user == null) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok().headers(httpHeaders).body(userMatchDtoConverter.transform(userMatchService
                .findByUserName(userName)));

//        return ResponseEntity.ok().headers(httpHeaders).body(userMatchDtoConverter.transform(userMatchService.getAll()));
    }

    //TODO убрать в production
    @GetMapping(value = "/getAllByMatchMethod/{matchMethodString}", produces = "application/json")
    public ResponseEntity<List<UserMatchDto>> getAllByMatchMethod(@PathVariable String matchMethodString){
        MatchMethod matchMethod = MatchMethod.valueOf(matchMethodString.toUpperCase());

        return ResponseEntity.ok().headers(httpHeaders).body(userMatchDtoConverter.transform(userMatchService
                .findByMatchMethod(matchMethod)));
    }

    @GetMapping(value = "/{userName}/{matchMethodString}", produces = "application/json")
    public ResponseEntity<List<UserMatchDto>> getAllByUserNameAndMatchMethod(@PathVariable String userName
            , @PathVariable String matchMethodString){
        //TODO после подключения security, убрать @PathVariable String userName и использовать principal.getName()
//        if(principal.getName() == null ||
//                (!principal.getName().equals(userName) && !principal.getName().equals(userName2)) )
//            return ResponseEntity.badRequest().build();

        MatchMethod matchMethod = MatchMethod.valueOf(matchMethodString.toUpperCase());

        return ResponseEntity.ok().headers(httpHeaders).body(userMatchDtoConverter.transform(userMatchService
                .findByUserNameAndMatchMethod(userName, matchMethod)));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<HttpHeaders> handleException(RuntimeException ex, HttpServletRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("messageError", "Something wrong: " + ex.getMessage()
                + "; path: " + request.getServletPath());
        return ResponseEntity.badRequest().headers(httpHeaders).build();
    }
}
