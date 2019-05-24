package com.psycorp.controller.api;

import com.psycorp.exception.BadRequestException;
import com.psycorp.model.dto.SimpleUserDto;
import com.psycorp.model.dto.UserMatchDto;
import com.psycorp.model.dto.ValueProfileMatchingDto;
import com.psycorp.model.entity.User;
import com.psycorp.model.enums.MatchMethod;
import com.psycorp.model.objects.UserMatch;
import com.psycorp.service.UserAccountService;
import com.psycorp.service.UserMatchService;
import com.psycorp.service.UserService;
import com.psycorp.service.ValueProfileService;
import com.psycorp.сonverter.UserDtoConverter;
import com.psycorp.сonverter.UserMatchDtoConverter;
import com.psycorp.сonverter.ValueProfileMatchingDtoConverter;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/match")
@PropertySource("classpath:errormessages.properties")
@PropertySource(value = {"classpath:match/areacommentrussian.properties"}, encoding = "utf-8")
@PropertySource(value = {"classpath:match/scalescommentrussian.properties"}, encoding = "utf-8")
public class ApiUserMatchController {

    private final UserAccountService userAccountService;
    private final UserMatchService userMatchService;
    private final ValueProfileService valueProfileService;
    private final UserDtoConverter userDtoConverter;
    private final UserMatchDtoConverter userMatchDtoConverter;
    private final ValueProfileMatchingDtoConverter valueProfileMatchingDtoConverter;

    private HttpHeaders httpHeaders;

    @Autowired
    public ApiUserMatchController(UserAccountService userAccountService, UserMatchService userMatchService,
                                  ValueProfileService valueProfileService, UserDtoConverter userDtoConverter,
                                  UserMatchDtoConverter userMatchDtoConverter,
                                  ValueProfileMatchingDtoConverter valueProfileMatchingDtoConverter) {
        this.userAccountService = userAccountService;
        this.userMatchService = userMatchService;
        this.userDtoConverter = userDtoConverter;
        this.valueProfileService = valueProfileService;
        this.userMatchDtoConverter = userMatchDtoConverter;
        this.valueProfileMatchingDtoConverter = valueProfileMatchingDtoConverter;
        this.httpHeaders = new HttpHeaders();
    }

    @GetMapping("/getUsersForMatching")
    public ResponseEntity<List<SimpleUserDto>> getUsersForMatching() {
        List<User> users = userAccountService.getUsersForMatching();

        return ResponseEntity.ok().headers(httpHeaders)
                .body(userDtoConverter.transform(users));
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<UserMatchDto>> getAllMatching() {
//        Map<AccountType,
        return ResponseEntity.ok().headers(httpHeaders).body(userMatchDtoConverter.transform(userMatchService.getAll()));
    }

    @PostMapping("/value-profile-for-matching")
    public ResponseEntity<ValueProfileMatchingDto> getValueProfilesForMatching(
            @RequestBody @NotNull @Valid ObjectId userId,
            @RequestHeader(value = "userForMatchingToken", required = false) String userForMatchingToken)
                            throws BadRequestException {
        return ResponseEntity.ok().headers(httpHeaders).body(valueProfileMatchingDtoConverter.transform(
                valueProfileService.getValueProfileForMatching(userId)));
    }

    //TODO надо передавать лист с users для тех, с кем идет сравнение
    @PostMapping(value = "/Pearson")
    public ResponseEntity<UserMatchDto> getUserMatchPearson(@RequestBody(required = false) SimpleUserDto userDto){

        User user = (userDto != null) ? userDtoConverter.transform(userDto) : null;
        return ResponseEntity.created(httpHeaders.getLocation())
                .headers(httpHeaders).body(userMatchDtoConverter.transform(userMatchService
                        .match(user, MatchMethod.PEARSONCORRELATION)));
    }

    @PostMapping(value = "/Percent")
    public ResponseEntity<UserMatchDto> getUserMatchPercent(@RequestBody(required = false) SimpleUserDto userDto){

        User user = (userDto != null) ? userDtoConverter.transform(userDto) : null;
        UserMatch userMatch = userMatchService.match(user, MatchMethod.PERCENT);
        UserMatchDto userMatchDto = userMatchDtoConverter.transform(userMatch);
        return ResponseEntity.created(httpHeaders.getLocation())
                .headers(httpHeaders).body(userMatchDto);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<HttpHeaders> handleException(RuntimeException ex, HttpServletRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("messageError", "Something wrong: " + ex.getMessage()
                + "; path: " + request.getServletPath());
        return ResponseEntity.badRequest().headers(httpHeaders).build();
    }
}
