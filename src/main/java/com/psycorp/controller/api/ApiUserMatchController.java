package com.psycorp.controller.api;

import com.psycorp.model.dto.UserMatchDto;
import com.psycorp.model.enums.MatchMethod;
import com.psycorp.service.UserMatchService;
import com.psycorp.service.UserService;
import com.psycorp.сonverter.UserMatchDtoConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/match")
@PropertySource("classpath:errormessages.properties")
public class ApiUserMatchController {

    private final UserService userService;
    private final UserMatchService userMatchService;
    private final UserMatchDtoConverter userMatchDtoConverter;
    private HttpHeaders httpHeaders;

    @Autowired
    public ApiUserMatchController(UserService userService, UserMatchService userMatchService,
                             UserMatchDtoConverter userMatchDtoConverter) {
        this.userService = userService;
        this.userMatchService = userMatchService;
        this.userMatchDtoConverter = userMatchDtoConverter;
    }

    @PostMapping(value = "/Pearson/{userName1}/{userName2}", produces = "application/json")
    public ResponseEntity<UserMatchDto> getUserMatchPearson(@PathVariable("userName1") String userName1
            , @PathVariable("userName2") String userName2, Principal principal){

//        if(principal.getName() == null ||
//                (!principal.getName().equals(userName1) && !principal.getName().equals(userName2)) )
//            return ResponseEntity.badRequest().build();
        return ResponseEntity.created(httpHeaders.getLocation())
                .headers(httpHeaders).body(userMatchDtoConverter.transform(userMatchService
                .match(userService.findFirstUserByName(userName1), userService.findFirstUserByName(userName2)
                        , MatchMethod.PEARSONCORRELATION)));
    }

    @PostMapping(value = "/Percent/{userName1}/{userName2}", produces = "application/json")
    public ResponseEntity<UserMatchDto> getUserMatchPercent(@PathVariable("userName1") String userName1
            , @PathVariable("userName2") String userName2, Principal pr){
//        if(principal.getName() == null ||
//                (!principal.getName().equals(userName1) && !principal.getName().equals(userName2)) )
//            return ResponseEntity.badRequest().build();
        return ResponseEntity.created(httpHeaders.getLocation())
                .headers(httpHeaders).body(userMatchDtoConverter.transform(userMatchService
                .match(userService.findFirstUserByName(userName1), userService.findFirstUserByName(userName2)
                        , MatchMethod.PERCENT)));
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
