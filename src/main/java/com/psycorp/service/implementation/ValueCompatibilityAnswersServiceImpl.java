package com.psycorp.service.implementation;

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
import java.util.*;

import static com.psycorp.security.SecurityConstant.ACCESS_TOKEN_PREFIX;

/**
 * Service implementation for ValueCompatibilityAnswersService.
 * @author  Maryna Kontar
 */
@Service
public class ValueCompatibilityAnswersServiceImpl implements ValueCompatibilityAnswersService {

    private final ValueCompatibilityAnswersRepository valueCompatibilityAnswersRepository;
    private final UserService userService;
    private final TokenService tokenService;
    private final MongoOperations mongoOperations;
    private final Environment env;

    //TODO return in public methods ValueCompatibilityAnswers (create in /objects)
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

    @Override
    public ValueCompatibilityAnswersEntity getInitValueCompatibilityAnswers(){
        ValueCompatibilityAnswersEntity answersEntity = new ValueCompatibilityAnswersEntity();
        answersEntity.setUserAnswers(choiceList());
        return answersEntity;
    }

    @Override
    @Transactional
    public ValueCompatibilityAnswersEntity saveFirstPartOfTests(String token,
                                                                String userForMatchingToken,
                                                                ValueCompatibilityAnswersEntity answersEntity,
                                                                List<Choice> choices, Area area) {

        User principal;
       if (token == null) {
           principal = userService.createAnonimUser(); // если токен == null, то создаем анонимного пользователя
           token = ACCESS_TOKEN_PREFIX + " " + tokenService.generateAccessTokenForAnonim(principal).getToken();
       } else {
           tokenService.changeInviteTokenToAccess(token);
       }

       return saveChoices(token, userForMatchingToken, answersEntity, choices, area);
    }

    @Override
    @Transactional
    public ValueCompatibilityAnswersEntity saveChoices(String token,
                                                       String userForMatchingToken,
                                                       ValueCompatibilityAnswersEntity answersEntity,
                                                       List<Choice> choices,
                                                       Area area){
        validateChoices(choices, area);
        User user = getUserByToken(token);
        addUserForMatching(userForMatchingToken, user);
        answersEntity = valueCompatibilityAnswersRepository
                .findTopByUserIdAndPassedOrderByPassDateDesc(user.getId(), false)
                .map((answers) -> update(answers, choices, area))
                .orElseGet(() -> insert(choices, user));
        return answersEntity;
    }

    @Override
    public ValueCompatibilityAnswersEntity getLastPassedTest(User user) {
        if(user != null && user.getId() != null) {
            return valueCompatibilityAnswersRepository.findTopByUserIdAndPassedOrderByPassDateDesc(user.getId(), true)
                    .orElseThrow(() -> new BadRequestException(env.getProperty("error.noPassedValueCompatibilityAnswersFind")));
        } else throw new BadRequestException(env.getProperty("error.UserOrUserIdCan`tBeNull"));
    }

    @Override
    public Boolean ifTestPassed(ObjectId userId) {
        Boolean ifTestPassed = valueCompatibilityAnswersRepository.existsByUserIdAndPassed(userId, true);
        return ifTestPassed;
    }

    private User getUserByToken(String token) {
        // if the user visits the app using the link with the token, then we are looking for the user by this token
        if(token != null && !token.isEmpty()) {
           return tokenService.getUserByToken(token.substring(ACCESS_TOKEN_PREFIX.length() + 1));
        }
        return  userService.getPrincipalUser();
    }

    private void addUserForMatching(String userForMatchingToken, User user) {
        if (userForMatchingToken != null && !userForMatchingToken.isEmpty()) {
            User userForMatching = getUserByToken(userForMatchingToken);
            userService.addNewUsersForMatching(user, Collections.singletonList(userForMatching), Update.Position.FIRST);
            userService.addNewUsersForMatching(userForMatching, Collections.singletonList(user), Update.Position.FIRST);
        }
    }

    private ValueCompatibilityAnswersEntity update(ValueCompatibilityAnswersEntity answersEntity,
                                                   List<Choice> choices,
                                                   Area area) {

        // засетить юзера, если был аноним, а стал нормальным principal

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
        if (isPassed(answersEntity)) { // если тест пройден, то установить поле "passed" в ValueCompatibilityAnswersEntity = true
            answersEntity = mongoOperations.findAndModify(query, new Update().set("passed", true),
                    new FindAndModifyOptions().returnNew(true), ValueCompatibilityAnswersEntity.class);
        }
        return answersEntity;
    }

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

    //TODO Проверить и переделать
    private void canPassTestAgain(User user) {
        if(user.getId() != null) {
//            Optional<List<ValueCompatibilityAnswersEntity>> answersOptional = valueCompatibilityAnswersRepository.findAllByUserIdOrderByIdDesc(user.getId());
            Optional<ValueCompatibilityAnswersEntity> answersEntityOptional =
                    valueCompatibilityAnswersRepository.findTopByUserIdOrderByPassDateDesc(user.getId());

            if(answersEntityOptional.isPresent()){
                ValueCompatibilityAnswersEntity answersEntity = answersEntityOptional.get();
            }

//            if (userAnswersOptional.isPresent() && userAnswersOptional.get().isEmpty()) {
//                ValueCompatibilityAnswersEntity answersEntity = userAnswersOptional.get().get(0);

            //TODO вернуть в рабочий вид
//           if(Period.between(answersEntity.getPassDate().toLocalDate(), LocalDateTime.now().toLocalDate()).getDays()
//                   <= Period.ofMonths(6).getDays()) throw new BadRequestException(env.getProperty("error.YouCan'tPassNewTest"));
        }
    }

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

    private void validateChoices(List<Choice> choices, Area area) {
        Integer numberOfQuestions = Integer.valueOf(env.getProperty("total.number.of.questions"));
        if (choices.size() != numberOfQuestions) throw new BadRequestException(env.getProperty("error.ItMustBe15TestsForArea") + " " + area);
        if ( choices.stream().anyMatch(choice -> choice.getArea() != area) ) {
            throw new BadRequestException(env.getProperty("error.AreaMustBe") + " " + area);
        }
        if(choices.stream().anyMatch(choice -> choice.getChosenScale() == null))
            throw new BadRequestException(env.getProperty("error.ChosenScaleCan'tBeNull") + " for area: " + area);
    }

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
