package com.psycorp.service.implementation;

import com.psycorp.exception.AuthorizationException;
import com.psycorp.exception.BadRequestException;
import com.psycorp.model.entity.*;
import com.psycorp.model.enums.Area;
import com.psycorp.model.enums.ErrorEnum;
import com.psycorp.model.enums.Scale;
import com.psycorp.model.enums.TokenType;
import com.psycorp.model.security.TokenEntity;
import com.psycorp.repository.ValueCompatibilityAnswersRepository;
import com.psycorp.repository.UserRepository;
import com.psycorp.security.token.TokenPrincipal;
import com.psycorp.service.ValueCompatibilityAnswersService;
import com.psycorp.service.UserService;
import com.psycorp.service.security.AuthService;
import com.psycorp.service.security.TokenService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
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

@Service
public class ValueCompatibilityAnswersServiceImpl implements ValueCompatibilityAnswersService {

    private final ValueCompatibilityAnswersRepository valueCompatibilityAnswersRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final UserService userService;
    @Autowired
    private TokenService tokenService;
    private final MongoOperations mongoOperations;
    private final Environment env;

    @Autowired
    public ValueCompatibilityAnswersServiceImpl(ValueCompatibilityAnswersRepository valueCompatibilityAnswersRepository, UserRepository userRepository,
                                                AuthService authService, UserService userService, MongoOperations mongoOperations,
                                                Environment env) {
        this.valueCompatibilityAnswersRepository = valueCompatibilityAnswersRepository;
        this.userRepository = userRepository;
        this.authService = authService;
        this.userService = userService;
        this.mongoOperations = mongoOperations;
        this.env = env;
    }

    @Override
    @Transactional
    public ValueCompatibilityAnswersEntity saveChoices(String token, ValueCompatibilityAnswersEntity answersEntity,
                                                       List<Choice> choices, Area area){
        validateChoices(choices, area);
        User user = getUserByToken(token);
        answersEntity = valueCompatibilityAnswersRepository.findTopByUser_IdAndPassedOrderByPassDateDesc(user.getId(), false)
                .map((answers) -> update(answers, choices))
                .orElseGet(() -> insert(choices, user));
        return answersEntity;
    }

    private void userAuthorization(ObjectId userId) {
        TokenPrincipal tokenPrincipal = (TokenPrincipal)(authService.getAuthPrincipal());
        if( !tokenPrincipal.getId().equals(userId) ) {
            //TODO какой exception кидать?
            throw new AuthorizationException("Access Denied", ErrorEnum.NOT_ENOUGH_PERMISSIONS);
        }
    }

    //TODO Проверить и переделать
    private void canPassTestAgain(User user) {
        if(user.getId() != null) {
//            Optional<List<ValueCompatibilityAnswersEntity>> answersOptional = valueCompatibilityAnswersRepository.findAllByUser_IdOrderByIdDesc(user.getId());
            Optional<ValueCompatibilityAnswersEntity> answersEntityOptional = valueCompatibilityAnswersRepository.findTopByUser_IdOrderByPassDateDesc(user.getId());

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

    private User getUserByToken(String token) {

        // если пользователь заходит на сайт по ссылке с токеном, то ищем пользователя по этому токену
        if(token != null && !token.isEmpty()) {
           return authService.getUserByToken(token.substring(ACCESS_TOKEN_PREFIX.length() + 1));
        }
        return  getPrincipal();
    }

    private User getPrincipal() {

        User principal;
        TokenPrincipal tokenPrincipal = (TokenPrincipal) authService.getAuthPrincipal();

        if(tokenPrincipal != null && tokenPrincipal.getId() != null) { //если есть токен
            principal = userService.findById(tokenPrincipal.getId());
        } else { principal = userService.createAnonimUser(); } // если токен == null или у него id == null, то создаем анонимного пользователя

        return principal;
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

    private ValueCompatibilityAnswersEntity insert(List<Choice> choices, User user) {
        canPassTestAgain(user);
        ValueCompatibilityAnswersEntity answersEntity = getInitValueCompatibilityAnswers();
        answersEntity.setUser(user);
        answersEntity.setUserAnswers(choices);
        answersEntity.setPassDate(LocalDateTime.now());
        answersEntity.setCreationDate(LocalDateTime.now());
        answersEntity.setPassed(isPassed(answersEntity));
        return valueCompatibilityAnswersRepository.insert(answersEntity);
    }

    private ValueCompatibilityAnswersEntity update(ValueCompatibilityAnswersEntity answersEntity, List<Choice> choices) {

        // засетить юзера, если был аноним, а стал нормальным principal

        // UPDATE CHOICES
        Update updateAnswers = new Update().set("passDate", LocalDateTime.now()).push("userAnswers").each(choices);
        Query query = Query.query(Criteria.where(Fields.UNDERSCORE_ID).is(answersEntity.getId()));
        mongoOperations.updateFirst(query, updateAnswers, ValueCompatibilityAnswersEntity.class);

        answersEntity = valueCompatibilityAnswersRepository.findById(answersEntity.getId())
                .orElseThrow(() -> new BadRequestException(env.getProperty("error.DataBaseError")));

        // UPDATE PASSED
        if (isPassed(answersEntity)) { // если тест пройден, то установить поле "passed" в ValueCompatibilityAnswersEntity = true
            mongoOperations.updateFirst(query, new Update().set("passed", true), ValueCompatibilityAnswersEntity.class);
        }
        answersEntity = valueCompatibilityAnswersRepository.findById(answersEntity.getId())
                .orElseThrow(() -> new BadRequestException(env.getProperty("error.DataBaseError")));

        return answersEntity;
    }

    @Override
    public ValueCompatibilityAnswersEntity findById(ObjectId id) {
        return valueCompatibilityAnswersRepository.findById(id)
                .orElseThrow(() -> new BadRequestException(env.getProperty("error.noValueCompatibilityAnswersFind")));
    }

    @Override
    public ValueCompatibilityAnswersEntity findLastValueCompatibilityAnswersByUserNameOrEmail(String userName) {

        User user = userService.findUserByNameOrEmail(userName);

        //TODO
        ValueCompatibilityAnswersEntity answersEntity;
        Optional<List<ValueCompatibilityAnswersEntity>> answersEntityOptional = valueCompatibilityAnswersRepository
                .findAllByUser_IdOrderByIdDesc(user.getId());
        Boolean userAnswersIsPresent = !answersEntityOptional.get().isEmpty();
        if(userAnswersIsPresent) {
            answersEntity = valueCompatibilityAnswersRepository
                    .findAllByUser_IdOrderByIdDesc(user.getId()).get().get(0);
        } else answersEntity = getInitValueCompatibilityAnswers();
        return answersEntity;
    }

    @Override
    public List<ValueCompatibilityAnswersEntity> findAllByUserNameOrderByCreationDateDesc(String userName) {

        User user = userService.findUserByNameOrEmail(userName);

        //TODO может возникнуть проблема, если в _id в ValueCompatibilityAnswersEntity ObjectId когда-нибудь повторится.
        // Вроде не должно, так как ObjectId  ("OrderById") отсортирован по дате создания
        // правда с точностью 1 сек. Плюс - не надо индекс на PassDate/CreationDate в ValueCompatibilityAnswersEntity создавать
        return valueCompatibilityAnswersRepository.findAllByUser_IdOrderByIdDesc(user.getId())
                .orElseThrow(() ->new BadRequestException(env.getProperty("error.noValueCompatibilityAnswersFind")
                        + " for user with name: " + userName));
    }

    @Override
    public ValueCompatibilityAnswersEntity getLastPassedTest() {
        User user = getPrincipal();
        return valueCompatibilityAnswersRepository.findTopByUser_IdAndPassedOrderByPassDateDesc(user.getId(),true)
                .orElseThrow(() -> new BadRequestException(env.getProperty("error.noPassedValueCompatibilityAnswersFind")));
    }

    @Override
    public void changeInviteTokenToAccess(String token) {
        if (tokenService.ifExistByTypeAndToken(TokenType.INVITE_TOKEN, token)) {
            TokenEntity tokenEntity = tokenService.getByTypeAndToken(TokenType.INVITE_TOKEN, token);

            // UPDATE TOKENTYPE and EXPIRATIONDATE
            Update update = new Update().set("type", TokenType.ACCESS_TOKEN)
                                        .set("expirationDate", tokenService.getTokenExpirationDate(TokenType.ACCESS_TOKEN));
            Query query = Query.query(Criteria.where(Fields.UNDERSCORE_ID).is(tokenEntity.getId()));
            mongoOperations.updateFirst(query, update, TokenEntity.class);
        }
    }

    @Override
    public ValueCompatibilityAnswersEntity getLastPassedTest(User user) {
        if(user != null && user.getId() != null) {
            return valueCompatibilityAnswersRepository.findTopByUser_IdAndPassedOrderByPassDateDesc(user.getId(), true)
                    .orElseThrow(() -> new BadRequestException(env.getProperty("error.noPassedValueCompatibilityAnswersFind")));
        } else throw new BadRequestException(env.getProperty("error.UserOrUserIdCan`tBeNull"));
    }

    @Override
    public Boolean ifTestPassed(User user) {

        Optional<ValueCompatibilityAnswersEntity> answersEntity = valueCompatibilityAnswersRepository
                .findTopByUser_IdOrderByPassDateDesc(user.getId());
        if(answersEntity.isPresent()) {
            return answersEntity.get().getPassed();
        } else return false;
    }

    @Override
    public ValueCompatibilityAnswersEntity getInitValueCompatibilityAnswers(){
        ValueCompatibilityAnswersEntity answersEntity = new ValueCompatibilityAnswersEntity();
        answersEntity.setUserAnswers(choiceList());
        return answersEntity;
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
//        SecureRandom random = new SecureRandom();
//        Collections.shuffle(testing, random);
        Collections.shuffle(scales);

        //set random first and second test scale
//        Boolean random = new Random(454567).nextBoolean();
//        Scale one = random ? scaleOne : scaleTwo;
//        Scale two = random ? scaleTwo : scaleOne;
//
//        choice.setFirstScale(one);
//        choice.setSecondScale(two);

        choice.setFirstScale(scales.get(0));
        choice.setSecondScale(scales.get(1));

        return choice;
    }

    private User validateUserByUserName(User user){
        return userRepository.findFirstByName(user.getName()).orElseThrow(() ->
               new BadRequestException(env.getProperty("error.UserAlreadyExists") + "with user name: " + user.getName()));
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

}
