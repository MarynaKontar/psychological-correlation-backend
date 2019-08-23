package com.psycorp.service.implementation;

import com.psycorp.exception.AuthorizationException;
import com.psycorp.exception.BadRequestException;
import com.psycorp.model.entity.*;
import com.psycorp.model.enums.Area;
import com.psycorp.model.enums.Scale;
import com.psycorp.repository.ValueCompatibilityAnswersRepository;
import com.psycorp.service.ValueCompatibilityAnswersService;
import com.psycorp.service.UserService;
import com.psycorp.service.security.TokenService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;

import static com.psycorp.security.SecurityConstant.ACCESS_TOKEN_PREFIX;

/**
 * Service implementation for ValueCompatibilityAnswersService.
 * @author Maryna Kontar
 */
@Service
public class ValueCompatibilityAnswersServiceImpl implements ValueCompatibilityAnswersService {

    private final ValueCompatibilityAnswersRepository valueCompatibilityAnswersRepository;
    private final UserService userService;
    private final TokenService tokenService;
    private final MongoOperations mongoOperations;
    private final Environment env;

    //TODO Create ValueCompatibilityAnswers (in layer objects)  and return it there in public methods
    @Autowired
    public ValueCompatibilityAnswersServiceImpl(ValueCompatibilityAnswersRepository valueCompatibilityAnswersRepository,
                                                UserService userService,
                                                TokenService tokenService, MongoOperations mongoOperations,
                                                Environment env) {
        this.valueCompatibilityAnswersRepository = valueCompatibilityAnswersRepository;
        this.userService = userService;
        this.tokenService = tokenService;
        this.mongoOperations = mongoOperations;
        this.env = env;
    }

    /**
     * Gets initial values for value compatibility test.
     * @return {@link ValueCompatibilityAnswersEntity} with initial values.
     */
    @Override
    public ValueCompatibilityAnswersEntity getInitValueCompatibilityAnswers(){
        ValueCompatibilityAnswersEntity answersEntity = new ValueCompatibilityAnswersEntity();
        answersEntity.setUserAnswers(choiceList());
        return answersEntity;
    }

    /**
     * Saves choices for given area for user that matches token or to principal user if token is {@literal null}.
     * @param token user token ()
     * @param userForMatchingToken token to add for matching.
     * @param choices must not be {@literal null}.
     * @param area must not be {@literal null}.
     * @return saved {@link ValueCompatibilityAnswersEntity}.
     */
    @Override
    @Transactional
    public ValueCompatibilityAnswersEntity saveFirstPartOfTests(String token,
                                                                String userForMatchingToken,
                                                                List<Choice> choices, Area area) {

        // or the user has already passed the test, or someone sent him a link with a token
        User principal;
       if (token == null) { //if user doesn't pass test yet, than create anonim user
           principal = userService.createAnonimUser();
           token = ACCESS_TOKEN_PREFIX + " " + tokenService.generateAccessTokenForAnonim(principal).getToken();
       } else {
           principal = getUserByToken(token);
           tokenService.changeInviteTokenToAccess(token);
       }
       addUserForMatching(userForMatchingToken, principal);
       return saveChoices(token, choices, area);
    }

    /**
     * Saves choices for given area to last not passed {@link ValueCompatibilityAnswersEntity}
     * (or to new one, if all tests are passed)
     * for user taken by token (if the user visits the app using the link with the token)
     * or for principal user, if token is {@literal null}.
     * @param choices must not be {@literal null}.
     * @param area must not be {@literal null}.
     * @return saved {@link ValueCompatibilityAnswersEntity}.
     */
    @Override
    @Transactional
    public ValueCompatibilityAnswersEntity saveChoices(String token, List<Choice> choices, Area area){
        validateChoices(choices, area);
        User user = (token != null && !token.isEmpty())? getUserByToken(token) : userService.getPrincipalUser();
        return valueCompatibilityAnswersRepository
                .findTopByUserIdAndPassedOrderByPassDateDesc(user.getId(), false)
                .map((answers) -> update(answers, choices, area))
                .orElseGet(() -> insert(choices, user));
    }

    /**
     * Gets last passed test for user.
     * @param user must not be {@literal null}.
     * @return last passed {@link ValueCompatibilityAnswersEntity} for given user.
     * @throws BadRequestException if user is {@literal null}
     * or if no passed {@link ValueCompatibilityAnswersEntity} is found.
     */
    @Override
    public ValueCompatibilityAnswersEntity getLastPassedTest(User user) {
        if(user != null && user.getId() != null) {
            return valueCompatibilityAnswersRepository.findTopByUserIdAndPassedOrderByPassDateDesc(user.getId(), true)
                    .orElseThrow(() -> new BadRequestException(env.getProperty("error.noPassedValueCompatibilityAnswersFind")));
        } else throw new BadRequestException(env.getProperty("error.UserOrUserIdCan`tBeNull"));
    }

    /**
     * Returns whether a value compatibility test passed for user with userId.
     * @param userId must not be {@literal null}.
     * @return {@literal true} if a value compatibility test passed
     * for user with userId, {@literal false} otherwise.
     * @throws IllegalArgumentException if {@code userId} is {@literal null}.
     */
    @Override
    public Boolean ifTestPassed(ObjectId userId) {
        return valueCompatibilityAnswersRepository.existsByUserIdAndPassed(userId, true);
    }

    /**
     * Gets user by token value.
     * @param token must not be {@literal null}.
     * @return {@link User} for given token.
     * @throws AuthorizationException if token is expired or not exists.
     * @throws BadRequestException if user not found.
     */
    private User getUserByToken(String token) {
        return tokenService.getUserByToken(token.substring(ACCESS_TOKEN_PREFIX.length() + 1));
    }

    /**
     * Adds user for matching retrieved by userForMatchingToken to principal and vise versa.
     * @param userForMatchingToken
     * @param principal must not be {@literal null}.
     */
    private void addUserForMatching(String userForMatchingToken, User principal) {
        if (userForMatchingToken != null && !userForMatchingToken.isEmpty()) {
            User userForMatching = getUserByToken(userForMatchingToken);
            userService.addNewUsersForMatching(principal, Collections.singletonList(userForMatching), Update.Position.FIRST);
            userService.addNewUsersForMatching(userForMatching, Collections.singletonList(principal), Update.Position.FIRST);
        }
    }

    /**
     * Updates choices for given area in answersEntity.
     * If some choices with given area already exists in answersEntity they will be deleted and updated by {@code choices}
     * @param answersEntity must not be {@literal null}.
     * @param choices must not be {@literal null}.
     * @param area must not be {@literal null}.
     * @return updated {@link ValueCompatibilityAnswersEntity}.
     */
    private ValueCompatibilityAnswersEntity update(ValueCompatibilityAnswersEntity answersEntity,
                                                   List<Choice> choices,
                                                   Area area) {

        Query query = Query.query(Criteria.where(Fields.UNDERSCORE_ID).is(answersEntity.getId()));

        // if there are answers with this area in answersEntity already, delete them
        if(answersEntity.getUserAnswers().stream().anyMatch(choice -> choice.getArea() == area)) {
            Update update = new Update().pull("userAnswers",  Query.query(Criteria.where("area").is(area)));
            mongoOperations.updateFirst(query, update, ValueCompatibilityAnswersEntity.class);
        }

        // UPDATE CHOICES
        Update updateAnswers = new Update()
                .set("passDate", LocalDateTime.now())
                .push("userAnswers").each(choices);
        answersEntity = mongoOperations.findAndModify(query, updateAnswers,
                new FindAndModifyOptions().returnNew(true), ValueCompatibilityAnswersEntity.class);// вернет уже измененный документ (returnNew(true))

        // UPDATE PASSED
        if (isPassed(answersEntity)) { // if test is passed, than make field "passed" in ValueCompatibilityAnswersEntity = true
            answersEntity = mongoOperations.findAndModify(query, new Update().set("passed", true),
                    new FindAndModifyOptions().returnNew(true), ValueCompatibilityAnswersEntity.class);
        }
        return answersEntity;
    }

    /**
     * Inserts choices to new {@link ValueCompatibilityAnswersEntity} for given user.
     * @param choices must not be {@literal null}.
     * @param user must not be {@literal null}.
     * @return new created {@link ValueCompatibilityAnswersEntity} with choices.
     */
    private ValueCompatibilityAnswersEntity insert(List<Choice> choices, User user) {
        canPassTestAgain(user);
        ValueCompatibilityAnswersEntity answersEntity = getInitValueCompatibilityAnswers();
        answersEntity.setUserId(user.getId());
        answersEntity.setUserAnswers(choices);
        answersEntity.setPassDate(LocalDateTime.now());
        answersEntity.setCreationDate(LocalDateTime.now());
        answersEntity.setPassed(isPassed(answersEntity));
        return valueCompatibilityAnswersRepository.insert(answersEntity);
    }

    /**
     * Determines if value compatibility test can be passed for user.
     * @param user must not be {@literal null}.
     * @throws BadRequestException if value compatibility test cann't be passed.
     */
    private void canPassTestAgain(User user) {
        Optional<ValueCompatibilityAnswersEntity> answersEntityOptional =
                valueCompatibilityAnswersRepository.findTopByUserIdOrderByPassDateDesc(user.getId());

        if(answersEntityOptional.isPresent()){
            ValueCompatibilityAnswersEntity answersEntity = answersEntityOptional.get();
            //TODO вернуть в рабочий вид
//            if(Period.between(answersEntity.getPassDate().toLocalDate(), LocalDateTime.now().toLocalDate()).getDays()
//                    <= Period.ofMonths(6).getDays()) throw new BadRequestException(env.getProperty("error.YouCan'tPassNewTest"));
        }
    }

    /**
     * Returns whether a value compatibility test can be marked as passed.
     * @param answersEntity must not be {@literal null}.
     * @return {@literal true} if a value compatibility test can be marked as passed, {@literal false} otherwise.
     */
    private Boolean isPassed(ValueCompatibilityAnswersEntity answersEntity) {
        Integer numberOfQuestions = Integer.valueOf(env.getProperty("total.number.of.questions"));
        List<Choice> choices = answersEntity.getUserAnswers();
        if (choices.size() != 45) return false;
        if(choices.stream().filter(choice -> choice.getArea() == Area.GOAL).count() != numberOfQuestions
                || choices.stream().filter(choice -> choice.getArea() == Area.QUALITY).count() != numberOfQuestions
                || choices.stream().filter(choice -> choice.getArea() == Area.STATE).count() != numberOfQuestions
                ) return false;

        if(choices.stream().anyMatch(choice -> choice.getChosenScale() == null)) return false;

        return true;
    }

    /**
     * Validates if the choices size matches a specific value
     * and if all choice areas are area
     * and if all chosenScales in choices are not {@literal null}.
     * @param choices must not be {@literal null}.
     * @param area must not be {@literal null}.
     * @throws BadRequestException if choices are not valid.
     */
    private void validateChoices(List<Choice> choices, Area area) {
        Integer numberOfQuestions = Integer.valueOf(env.getProperty("total.number.of.questions"));
        if (choices.size() != numberOfQuestions) throw new BadRequestException(env.getProperty("error.ItMustBe15TestsForArea") + " " + area);
        if ( choices.stream().anyMatch(choice -> choice.getArea() != area) ) {
            throw new BadRequestException(env.getProperty("error.AreaMustBe") + " " + area);
        }
        if(choices.stream().anyMatch(choice -> choice.getChosenScale() == null))
            throw new BadRequestException(env.getProperty("error.ChosenScaleCan'tBeNull") + " for area: " + area);
    }

    /**
     * Returns initialize list of {@link Choice} with three {@link Area}: GOAL, QUALITY and STATE
     * for six {@link Scale}.
     * All firstScales and secondScales are filled, all chosenScales are empty.
     * Pairs of scales and choices for given area are shuffled.
     * @return shuffled initialize list of {@link Choice}.
     */
    private List<Choice> choiceList(){

        //GOAL
        List<Choice> choiceGoal = new ArrayList<>();
        choiceGoal.add(getChoice(Area.GOAL, Scale.ONE, Scale.TWO));
        choiceGoal.add(getChoice(Area.GOAL, Scale.ONE, Scale.THREE));
        choiceGoal.add(getChoice(Area.GOAL, Scale.ONE, Scale.FOUR));
        choiceGoal.add(getChoice(Area.GOAL, Scale.ONE, Scale.FIVE));
        choiceGoal.add(getChoice(Area.GOAL, Scale.ONE, Scale.SIX));

        choiceGoal.add(getChoice(Area.GOAL, Scale.TWO, Scale.THREE));
        choiceGoal.add(getChoice(Area.GOAL, Scale.TWO, Scale.FOUR));
        choiceGoal.add(getChoice(Area.GOAL, Scale.TWO, Scale.FIVE));
        choiceGoal.add(getChoice(Area.GOAL, Scale.TWO, Scale.SIX));

        choiceGoal.add(getChoice(Area.GOAL, Scale.THREE, Scale.FOUR));
        choiceGoal.add(getChoice(Area.GOAL, Scale.THREE, Scale.FIVE));
        choiceGoal.add(getChoice(Area.GOAL, Scale.THREE, Scale.SIX));

        choiceGoal.add(getChoice(Area.GOAL, Scale.FOUR, Scale.FIVE));
        choiceGoal.add(getChoice(Area.GOAL, Scale.FOUR, Scale.SIX));

        choiceGoal.add(getChoice(Area.GOAL, Scale.FIVE, Scale.SIX));

        Collections.shuffle(choiceGoal);


        //QUALITY
        List<Choice> choiceQuality = new ArrayList<>();
        choiceQuality.add(getChoice(Area.QUALITY, Scale.ONE, Scale.TWO));
        choiceQuality.add(getChoice(Area.QUALITY, Scale.ONE, Scale.THREE));
        choiceQuality.add(getChoice(Area.QUALITY, Scale.ONE, Scale.FOUR));
        choiceQuality.add(getChoice(Area.QUALITY, Scale.ONE, Scale.FIVE));
        choiceQuality.add(getChoice(Area.QUALITY, Scale.ONE, Scale.SIX));

        choiceQuality.add(getChoice(Area.QUALITY, Scale.TWO, Scale.THREE));
        choiceQuality.add(getChoice(Area.QUALITY, Scale.TWO, Scale.FOUR));
        choiceQuality.add(getChoice(Area.QUALITY, Scale.TWO, Scale.FIVE));
        choiceQuality.add(getChoice(Area.QUALITY, Scale.TWO, Scale.SIX));

        choiceQuality.add(getChoice(Area.QUALITY, Scale.THREE, Scale.FOUR));
        choiceQuality.add(getChoice(Area.QUALITY, Scale.THREE, Scale.FIVE));
        choiceQuality.add(getChoice(Area.QUALITY, Scale.THREE, Scale.SIX));

        choiceQuality.add(getChoice(Area.QUALITY, Scale.FOUR, Scale.FIVE));
        choiceQuality.add(getChoice(Area.QUALITY, Scale.FOUR, Scale.SIX));

        choiceQuality.add(getChoice(Area.QUALITY, Scale.FIVE, Scale.SIX));

        Collections.shuffle(choiceQuality);

        //STATE
        List<Choice> choiceState = new ArrayList<>();
        choiceState.add(getChoice(Area.STATE, Scale.ONE, Scale.TWO));
        choiceState.add(getChoice(Area.STATE, Scale.ONE, Scale.THREE));
        choiceState.add(getChoice(Area.STATE, Scale.ONE, Scale.FOUR));
        choiceState.add(getChoice(Area.STATE, Scale.ONE, Scale.FIVE));
        choiceState.add(getChoice(Area.STATE, Scale.ONE, Scale.SIX));

        choiceState.add(getChoice(Area.STATE, Scale.TWO, Scale.THREE));
        choiceState.add(getChoice(Area.STATE, Scale.TWO, Scale.FOUR));
        choiceState.add(getChoice(Area.STATE, Scale.TWO, Scale.FIVE));
        choiceState.add(getChoice(Area.STATE, Scale.TWO, Scale.SIX));

        choiceState.add(getChoice(Area.STATE, Scale.THREE, Scale.FOUR));
        choiceState.add(getChoice(Area.STATE, Scale.THREE, Scale.FIVE));
        choiceState.add(getChoice(Area.STATE, Scale.THREE, Scale.SIX));

        choiceState.add(getChoice(Area.STATE, Scale.FOUR, Scale.FIVE));
        choiceState.add(getChoice(Area.STATE, Scale.FOUR, Scale.SIX));

        choiceState.add(getChoice(Area.STATE, Scale.FIVE, Scale.SIX));

        Collections.shuffle(choiceState);


        List<Choice> choices = new ArrayList<>(choiceGoal);
        choices.addAll(choiceQuality);
        choices.addAll(choiceState);

        return choices;
    }

    /**
     * Gets choice for given area with shuffled firstScale and secondScale.
     * @param area must not be {@literal null}.
     * @param scaleOne must not be {@literal null}.
     * @param scaleTwo must not be {@literal null}.
     * @return {@link Choice} with shuffled firstScale and secondScale for given area.
     */
    private Choice getChoice(Area area, Scale scaleOne, Scale scaleTwo) {
        Choice choice = new Choice();
        choice.setArea(area);

        List<Scale> scales = Arrays.asList(scaleOne, scaleTwo);
        Collections.shuffle(scales);

        choice.setFirstScale(scales.get(0));
        choice.setSecondScale(scales.get(1));

        return choice;
    }

}
