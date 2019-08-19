package com.psycorp.controller.api;

import com.psycorp.model.dto.SimpleUserDto;
import com.psycorp.model.dto.ValueCompatibilityAnswersDto;
import com.psycorp.model.dto.ValueProfileIndividualDto;
import com.psycorp.model.entity.Choice;
import com.psycorp.model.entity.User;
import com.psycorp.model.entity.ValueCompatibilityAnswersEntity;
import com.psycorp.model.enums.Area;
import com.psycorp.model.objects.ValueProfileIndividual;
import com.psycorp.service.ValueCompatibilityAnswersService;
import com.psycorp.service.ValueProfileService;
import com.psycorp.service.security.TokenService;
import com.psycorp.сonverter.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

import static com.psycorp.security.SecurityConstant.ACCESS_TOKEN_PREFIX;

/**
 * Rest controller for value compatibility testing.
 * url : "/test"
 * @author Maryna Kontar
 */
@RestController
@RequestMapping("/test")
@PropertySource("classpath:errormessages.properties")
@PropertySource(value = {"classpath:testing/scalesquestionsukrainian.properties"}, encoding = "utf-8", ignoreResourceNotFound = true)
@PropertySource(value = {"classpath:testing/scalesquestionsrussian.properties"}, encoding = "utf-8")
@PropertySource(value = {"classpath:testing/scalesquestionsenglish.properties"}, encoding = "utf-8", ignoreResourceNotFound = true)
@PropertySource(value = {"classpath:valueprofile/scalescommentrussian.properties"}, encoding = "utf-8")
public class ApiValueCompatibilityAnswersController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiValueCompatibilityAnswersController.class);

    private final ValueCompatibilityAnswersService valueCompatibilityAnswersService;
    private final ValueProfileService valueProfileService;
    private final ValueProfileIndividualDtoConverter valueProfileIndividualDtoConverter;
    private final ChoiceDtoConverter choiceDtoConverter;
    private final UserDtoConverter userDtoConverter;
    private final ValueCompatibilityAnswersDtoConverter valueCompatibilityAnswersDtoConverter;
    private final TokenService tokenService;
    private HttpHeaders httpHeaders;

    @Autowired
    public ApiValueCompatibilityAnswersController(ValueCompatibilityAnswersService valueCompatibilityAnswersService,
                                                  ValueProfileService valueProfileService,
                                                  ValueProfileIndividualDtoConverter valueProfileIndividualDtoConverter,
                                                  ChoiceDtoConverter choiceDtoConverter, UserDtoConverter userDtoConverter,
                                                  ValueCompatibilityAnswersDtoConverter valueCompatibilityAnswersDtoConverter,
                                                  TokenService tokenService) {
        this.valueCompatibilityAnswersService = valueCompatibilityAnswersService;
        this.valueProfileService = valueProfileService;
        this.valueProfileIndividualDtoConverter = valueProfileIndividualDtoConverter;
        this.choiceDtoConverter = choiceDtoConverter;
        this.userDtoConverter = userDtoConverter;
        this.valueCompatibilityAnswersDtoConverter = valueCompatibilityAnswersDtoConverter;
        this.tokenService = tokenService;
        this.httpHeaders = new HttpHeaders();
    }

    /**
     * Endpoint for url ":/test/initTest".
     * Get tests questions and answer variants for all {@link Area} and
     * {@link com.psycorp.model.enums.Scale} for value compatibility test.
     * Order of pairs of scales is random for each area.
     * @return ValueCompatibilityAnswersDto with shuffled scales for each area and empty chosen scales.
     */
    @GetMapping("/initTest")
    public ResponseEntity<ValueCompatibilityAnswersDto> getInitValueCompatibilityAnswers(){
        LOGGER.trace("getInitValueCompatibilityAnswers");
        return ResponseEntity.ok().headers(httpHeaders).body(valueCompatibilityAnswersDtoConverter
                .transform(valueCompatibilityAnswersService.getInitValueCompatibilityAnswers()));
    }

    /**
     * Endpoint for url ":/test/goal".
     * Save results of value compatibility test for GOAL {@link Area}.
     * Only results with GOAL {@link Area} will be saved.
     * Results with QUALITY and STATE {@link Area} will be ignored.
     * <p>
     * If there is a token in the header, then the result is saved to the user with this token.
     * If there is no token, a new user is created, to whom the test result is saved.
     * <p>
     * If there is a userForMatchingToken in the header, then the user with this userForMatchingToken
     * is written to userForMatching of the principal user (in first place) and vise versa.
     * This is used when two users are tested on the same device without registering
     * themselves in the application (or registering) and want to immediately compare test results after testing
     * and then return to first user's account.
     * That is you can receive matching of test resuls for principal user and
     * user from first place in userForMatching with endpoint ":/match/Percent" (":/match/Pearson")
     * even if these users aren't registered
     * @see ApiAuthenticationController endpoint ":/auth/loginFriendAccount"
     * @see ApiUserMatchController endpoint ":/match/Percent"
     * @see ApiUserMatchController endpoint ":/match/Pearson"
     * @param valueCompatibilityAnswersDto value compatibility test results for GOAL {@link Area} to be saved.
     * @param token token of the user to whom the test results will be saved, if there is a token.
     * @param userForMatchingToken token for recording to userForMatching of the principal user and vise versa.
     * @return saved value compatibility test results.
     */
    @PostMapping("/goal")
    public ResponseEntity<ValueCompatibilityAnswersDto> saveGoal(
            @RequestBody @NotNull @Valid ValueCompatibilityAnswersDto valueCompatibilityAnswersDto,
            @RequestHeader(value = "Authorization", required = false) String token, // или пользователь уже проходил тестирование, или ему выслали ссылку с токеном
            @RequestHeader(value = "userForMatchingToken", required = false) String userForMatchingToken){ // два пользователя тестируются на одном компьютере

        LOGGER.trace("saveGoal: {}",valueCompatibilityAnswersDto);
        ValueCompatibilityAnswersEntity valueCompatibilityAnswersEntity = valueCompatibilityAnswersDtoConverter.transform(valueCompatibilityAnswersDto);
        List<Choice> choices = choiceDtoConverter.transform(valueCompatibilityAnswersDto.getGoal());

        valueCompatibilityAnswersEntity = valueCompatibilityAnswersService.saveFirstPartOfTests(token, userForMatchingToken,
                valueCompatibilityAnswersEntity, choices, Area.GOAL);

        token = ACCESS_TOKEN_PREFIX + " " + tokenService.findByUserId(valueCompatibilityAnswersEntity.getUserId()).getToken();
        return ResponseEntity.status(HttpStatus.CREATED)
                .headers(httpHeaders)
                .header(HttpHeaders.AUTHORIZATION, token) // или сгенеренный токен или пришедший в хедере (уже с 'Bearer ')
                .body(valueCompatibilityAnswersDtoConverter.transform(valueCompatibilityAnswersEntity));
    }

    /**
     * Endpoint for url ":/test/quality".
     * Save results of value compatibility test for QUALITY {@link Area}.
     * Only results with QUALITY {@link Area} will be saved.
     * Results with GOAL and STATE {@link Area} will be ignored.
     * @param valueCompatibilityAnswersDto value compatibility test results for QUALITY {@link Area} to be saved.
     * @return saved value compatibility test results.
     */
    @PostMapping("/quality")
    public ResponseEntity<ValueCompatibilityAnswersDto> saveQuality(
            @RequestBody @NotNull @Valid ValueCompatibilityAnswersDto valueCompatibilityAnswersDto) {
        LOGGER.trace("saveQuality: {}", valueCompatibilityAnswersDto);
        ValueCompatibilityAnswersEntity valueCompatibilityAnswersEntity = valueCompatibilityAnswersDtoConverter.transform(valueCompatibilityAnswersDto);
        List<Choice> choices = choiceDtoConverter.transform(valueCompatibilityAnswersDto.getQuality());
        valueCompatibilityAnswersEntity = valueCompatibilityAnswersService
                .saveChoices(null, null, valueCompatibilityAnswersEntity, choices, Area.QUALITY);
        return ResponseEntity.ok()
                .headers(httpHeaders)
                .body(valueCompatibilityAnswersDtoConverter.transform(valueCompatibilityAnswersEntity));
    }

    /**
     * Endpoint for url ":/test/state".
     * Save results of value compatibility test for STATE {@link Area}.
     * Only results with STATE {@link Area} will be saved.
     * Results with GOAL and QUALITY {@link Area} will be ignored.
     * @param valueCompatibilityAnswersDto value compatibility test results for STATE {@link Area} to be saved.
     * @return saved value compatibility test results.
     */
    @PostMapping("/state")
    public ResponseEntity<ValueCompatibilityAnswersDto> saveState(
            @RequestBody @NotNull @Valid ValueCompatibilityAnswersDto valueCompatibilityAnswersDto) {
        LOGGER.trace("saveState: {}", valueCompatibilityAnswersDto);
        ValueCompatibilityAnswersEntity valueCompatibilityAnswersEntity = valueCompatibilityAnswersDtoConverter.transform(valueCompatibilityAnswersDto);
        List<Choice> choices = choiceDtoConverter.transform(valueCompatibilityAnswersDto.getState());
        valueCompatibilityAnswersEntity = valueCompatibilityAnswersService
                .saveChoices(null, null, valueCompatibilityAnswersEntity, choices, Area.STATE);
        return ResponseEntity.ok()
                .headers(httpHeaders)
                .body(valueCompatibilityAnswersDtoConverter.transform(valueCompatibilityAnswersEntity));
    }

    /**
     * Endpoint for url ":/test/generateTokenList".
     * Generates a list of tokens for the principal user, which he can use in order to invite a friend
     * to be tested by adding a token as a parameter to the app link.
     * See "Authorization" key in RequestHeader for endpoint ":/test/goal".
     * An anonim user is created for each token in the database.
     * @return list of tokens for inviting friends to test.
     */
    @GetMapping("/generateTokenList")
    public ResponseEntity<List<String>> generateInviteTokenList(){
        LOGGER.trace("generateInviteTokenList");
        return ResponseEntity.ok()
                .headers(httpHeaders)
                .body(tokenService.generateInviteTokenList(3));
    }

    /**
     * Endpoint for url ":/test/value-profile".
     * Receives a value compatibility profile for a user from a userDto,
     * created based on the results of the last passed value compatibility test.
     * If there isn't userDto, then the profile is created for the principal user.
     * @param userDto user for whom the value compatibility profile is being created.
     * @return value compatibility profile for a user from a userDto.
     */
    @PostMapping("/value-profile")
    public ResponseEntity<ValueProfileIndividualDto> getValueProfile(@RequestBody(required = false)
                                                                     @NotNull @Valid SimpleUserDto userDto){
        LOGGER.trace("getValueProfile: {}", userDto);
        User user = (userDto != null) ? userDtoConverter.transform(userDto) : null;
        ValueProfileIndividual valueProfile = valueProfileService.getValueProfileIndividual(user);
        ValueProfileIndividualDto valueProfileIndividualDto = valueProfileIndividualDtoConverter.transform(valueProfile);
        return ResponseEntity.ok()
                .headers(httpHeaders)
                .body(valueProfileIndividualDto);
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
