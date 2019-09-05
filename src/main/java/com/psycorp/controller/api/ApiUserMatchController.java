package com.psycorp.controller.api;

import com.psycorp.model.dto.SimpleUserDto;
import com.psycorp.model.dto.UserMatchDto;
import com.psycorp.model.dto.ValueProfileMatchingDto;
import com.psycorp.model.entity.User;
import com.psycorp.model.enums.MatchMethod;
import com.psycorp.model.objects.UserMatch;
import com.psycorp.service.UserAccountService;
import com.psycorp.service.UserMatchService;
import com.psycorp.service.ValueProfileService;
import com.psycorp.сonverter.UserDtoConverter;
import com.psycorp.сonverter.UserMatchDtoConverter;
import com.psycorp.сonverter.ValueProfileMatchingDtoConverter;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Rest controller for matching tests.
 * url : "/match"
 * @author Maryna Kontar
 */
@RestController
@RequestMapping("/match")
@PropertySource("classpath:errormessages.properties")
@PropertySource(value = {"classpath:match/areacommentrussian.properties"}, encoding = "utf-8")
@PropertySource(value = {"classpath:match/scalescommentrussian.properties"}, encoding = "utf-8")
public class ApiUserMatchController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiUserMatchController.class);

    private final UserAccountService userAccountService;
    private final UserMatchService userMatchService;
    private final ValueProfileService valueProfileService;
    private final UserDtoConverter userDtoConverter;
    private final UserMatchDtoConverter userMatchDtoConverter;
    private final ValueProfileMatchingDtoConverter valueProfileMatchingDtoConverter;

    private HttpHeaders httpHeaders;

    @Autowired
    public ApiUserMatchController(UserAccountService userAccountService,
                                  UserMatchService userMatchService,
                                  ValueProfileService valueProfileService,
                                  UserDtoConverter userDtoConverter,
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


    /**
     * Endpoint for url ":/match/getUsersForMatching".
     * Gets users for matching with principal user.
     * If there is userForMatchingToken than return singleton list with user
     * that has this userForMatchingToken.
     * If no userForMatchingToken, than return all usersForMatching for principal user.
     * @return singleton list with user that has this userForMatchingToken
     * if there is userForMatchingToken; or all usersForMatching for principal user,
     * if there isn't userForMatchingToken.
     */
    @GetMapping("/getUsersForMatching")
    public ResponseEntity<List<SimpleUserDto>> getUsersForMatching(
            @RequestHeader(value = "userForMatchingToken", required = false) String userForMatchingToken) {
        LOGGER.trace("getUsersForMatching: {}", userForMatchingToken);
        List<User> users = userAccountService.getUsersForMatching(userForMatchingToken);
        return ResponseEntity.ok()
                .headers(httpHeaders)
                .body(userDtoConverter.transform(users));
    }

    /**
     * Endpoint for url ":/match/value-profile-for-matching".
     * Creates value compatibility profiles for user with userId and principal user
     * with comments that describe differences between that profiles.
     * Profiles are creating based on the results of the last passed value compatibility test.
     * @param userId id of the user for whom the matching profile will be created with the principal user.
     * @return ValueProfileMatchingDto for user with userId and principal user.
     */
    @PostMapping("/value-profile-for-matching")
    public ResponseEntity<ValueProfileMatchingDto> getValueProfilesForMatching(
                                @RequestBody @NotNull @Valid ObjectId userId) {
        LOGGER.trace("getValueProfilesForMatching: {}", userId);
        return ResponseEntity.ok()
                .headers(httpHeaders)
                .body(valueProfileMatchingDtoConverter.transform(
                        valueProfileService.getValueProfileForMatching(userId)));
    }

    /**
     * Endpoint for url ":/match/Pearson".
     * Gets a matching of test results for user from userDto and principal user using Pearson {@link MatchMethod}.
     * If there isn't userDto, matching is created for first user from userForMatching of principal user.
     * @param userDto user for whom matching with principal user is being created.
     * @return UserMatchDto with result of matching for user from userDto and principal user
     * created by Pearson {@link MatchMethod}
     */
    @PostMapping(value = "/Pearson")
    public ResponseEntity<UserMatchDto> getUserMatchPearson(@RequestBody(required = false) SimpleUserDto userDto){
        LOGGER.trace("getUserMatchPearson: {}", userDto);
        User user = (userDto != null) ? userDtoConverter.transform(userDto) : null;
        return ResponseEntity.created(httpHeaders.getLocation())
                .headers(httpHeaders)
                .body(userMatchDtoConverter.transform(userMatchService
                        .match(user, MatchMethod.PEARSONCORRELATION)));
    }

    /**
     * Endpoint for url ":/match/Percent".
     * Gets a matching of test results for user from userDto and principal user using Percent {@link MatchMethod}.
     * If there isn't userDto, matching is created for first user from userForMatching of principal user.
     * @param userDto user for whom matching with principal user is being created.
     * @return UserMatchDto with result of matching for user from userDto and principal user
     * created by Percent {@link MatchMethod}
     */
    @PostMapping(value = "/Percent")
    public ResponseEntity<UserMatchDto> getUserMatchPercent(@RequestBody(required = false) SimpleUserDto userDto){
        LOGGER.trace("getUserMatchPercent: {}", userDto);
        User user = (userDto != null) ? userDtoConverter.transform(userDto) : null;
        UserMatch userMatch = userMatchService.match(user, MatchMethod.PERCENT);
        UserMatchDto userMatchDto = userMatchDtoConverter.transform(userMatch);
        return ResponseEntity.created(httpHeaders.getLocation())
                .headers(httpHeaders).body(userMatchDto);
    }

    /**
     * RuntimeException handler.
     * @param ex RuntimeException for handling.
     * @param request HttpServletRequest which caused the RuntimeException.
     * @return ResponseEntity with HttpStatus BAD_REQUEST and exception message in header.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<HttpHeaders> handleException(RuntimeException ex, HttpServletRequest request) {
        LOGGER.trace("IP: {}:{}:{} : EXCEPTION: {}", request.getRemoteHost(), request.getRemotePort(), request.getRemoteUser(), ex);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("messageError", "Something wrong: " + ex.getMessage()
                        + "; path: " + request.getServletPath());
        return ResponseEntity.badRequest().headers(httpHeaders).build();
    }
}
