package com.psycorp.service.implementation;

import com.psycorp.exception.AuthorizationException;
import com.psycorp.exception.BadRequestException;
import com.psycorp.model.entity.Choice;
import com.psycorp.model.entity.User;
import com.psycorp.model.entity.UserAnswers;
import com.psycorp.model.enums.Area;
import com.psycorp.model.enums.ErrorEnum;
import com.psycorp.model.enums.Scale;
import com.psycorp.repository.UserAnswersRepository;
import com.psycorp.repository.UserRepository;
import com.psycorp.repository.security.CredentialsRepository;
import com.psycorp.security.token.TokenPrincipal;
import com.psycorp.service.UserAnswersService;
import com.psycorp.service.UserService;
import com.psycorp.service.security.AuthService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.psycorp.security.SecurityConstant.ACCESS_TOKEN_PREFIX;
import static java.util.Map.Entry.comparingByKey;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

@Service
@PropertySource(value = {"classpath:scales/scalesukrainian.properties"}, encoding = "utf-8", ignoreResourceNotFound = true)
@PropertySource(value = {"classpath:scales/scalesrussian.properties"}, encoding = "utf-8")
@PropertySource(value = {"classpath:scales/scalesenglish.properties"}, encoding = "utf-8", ignoreResourceNotFound = true)
public class UserAnswersServiceImpl implements UserAnswersService {

    private final UserAnswersRepository userAnswersRepository;
    private final UserRepository userRepository;
    private final CredentialsRepository credentialsRepository;
    private final AuthService authService;
    private final UserService userService;
    private final MongoOperations mongoOperations;
    private final Environment env;

    @Autowired
    public UserAnswersServiceImpl(UserAnswersRepository userAnswersRepository, UserRepository userRepository,
                                  CredentialsRepository credentialsRepository, AuthService authService, UserService userService, MongoOperations mongoOperations, Environment env) {
        this.userAnswersRepository = userAnswersRepository;
        this.userRepository = userRepository;
        this.credentialsRepository = credentialsRepository;
        this.authService = authService;
        this.userService = userService;
        this.mongoOperations = mongoOperations;
        this.env = env;
    }

    @Override
    @Transactional
    public UserAnswers saveChoices(String token, UserAnswers userAnswers, List<Choice> choices, Area area){

        validateChoices(choices, area);

        //GET USER
        User user = getUser(token);
//        Optional<UserAnswers> userAnswersOptional = userAnswersRepository.findTopByUser_IdAndPassedOrderByPassDateDesc(user.getId(), false);
//        Optional<UserAnswers> userAnswersOptional1 =userAnswersRepository.findTopByUser_IdAndPassedOrderByPassDateDesc(user.getId(), false)
//                .map((ua) -> update(ua, choices));
        userAnswers = userAnswersRepository.findTopByUser_IdAndPassedOrderByPassDateDesc(user.getId(), false)
                .map((ua) -> update(ua, choices))
                .orElseGet(() -> insert(choices, user));
//        userAnswers = userAnswersRepository.findAllByUser_IdAndPassedAndLastPassDate(user.getId(), false)
//                .map((ua) -> update(ua, choices))
//                .orElse(insert(choices, user));

        //UPDATE
        // если есть userAnswers у user, то вытянем его по userId и сохраним в него choices (если не isPassed)
        // или сохраним choices в новый UserAnswers (если isPassed)
//        if(!userAnswersRepository.findAllByUser_IdAndPassedOrderByPassDateDesc(user.getId(), false).isEmpty()) {
//            List<UserAnswers> userAnswersList = userAnswersRepository.findAllByUser_IdAndPassedOrderByPassDateDesc(user.getId(), false);
//            userAnswers = userAnswersList.get(0);
//
//            //проверяем, совпадают ли пользователь у userAnswers с user из principal, если нет - exception
//            //для нового юзера не пройдет - убрать
////            userAuthorization(userAnswers.getUser().getId());
//
//            userAnswers = update(userAnswers, choices);
//
//
//        //INSERT
//        // если последний тест уже пройден и прошел срок, после которого можно повторно пройти тест (если нет - exception)
//        } else {
//            userAnswers = insert(choices, user);
//            //что если прошел только часть тестов (goal, например), а потом заново начал через день?
//        }





//        //UPDATE
//        // если есть id у userAnswers, то даже если с фронта пришел какой-то не тот userAnswers, но с такой id,
//        // то вытянем по id и сохраним в него choices (если не isPassed) или сохраним choices в новый UserAnswers (если isPassed)
//        if(userAnswers.getId() != null && userAnswersRepository.findById(userAnswers.getId()).isPresent()) {
//            userAnswers = userAnswersRepository.findById(userAnswers.getId()).get(); // находим по id
//
//            if(!userAnswersRepository.findAllByUser_IdAndPassedOrderByPassDateDesc(user.getId(), false).isEmpty()) {
//                List<UserAnswers> userAnswersList = userAnswersRepository.findAllByUser_IdAndPassedOrderByPassDateDesc(user.getId(), false);
//                userAnswers = userAnswersList.get(0);
//            }
//            //проверяем, совпадают ли пользователь у userAnswers с user из principal, если нет - exception
//            userAuthorization(userAnswers.getUser().getId());
//
//            //если последний тест уже пройден (заменить после рефакторинга др. методов на userAnswers.getPassed)
//            // и прошел ли срок, после которого можно повторно пройти тест (если нет - exception) isCanPassTest
//            if(isPassed(userAnswers)) {
//                canPassTestAgain(user);
//                userAnswers = insert(choices, user);
//            }
//            userAnswers = update(userAnswers, choices);
//
//
//            //INSERT
//        }

        return userAnswers;
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
//            Optional<List<UserAnswers>> userAnswersOptional = userAnswersRepository.findAllByUser_IdOrderByIdDesc(user.getId());
            Optional<UserAnswers> userAnswersOptional1 = userAnswersRepository.findTopByUser_IdOrderByPassDateDesc(user.getId());

            if(userAnswersOptional1.isPresent()){
                UserAnswers userAnswers = userAnswersOptional1.get();
            }

//            if (userAnswersOptional.isPresent() && userAnswersOptional.get().isEmpty()) {
//                UserAnswers userAnswers = userAnswersOptional.get().get(0);
//           if(Period.between(userAnswers.getPassDate().toLocalDate(), LocalDateTime.now().toLocalDate()).getDays()
//                   <= Period.ofMonths(6).getDays()) throw new BadRequestException(env.getProperty("error.YouCan'tPassNewTest"));
        }
    }

    private User getUser(String token) {

        // если пользователь заходит на сайт по ссылке с токеном, то ищем пользователя по этому токену
        if(token != null && !token.isEmpty()) {
           return authService.getUserByToken(token.substring(ACCESS_TOKEN_PREFIX.length() + 1));
        }

        User user;
        TokenPrincipal tokenPrincipal = (TokenPrincipal) authService.getAuthPrincipal();

        if(tokenPrincipal != null && tokenPrincipal.getId() != null) { //если есть токен
            if(userRepository.findById(tokenPrincipal.getId()).isPresent()){ // и для него есть пользователь, то берем этого пользователя
            user = userRepository.findById(tokenPrincipal.getId()).get();
            } else { user = userService.createAnonimUser(); } // если для этого токена нет пользователя, то создаем анонимного (наверное надо кидать ошибку. Это случай, когда в бд не правильно сохраняли)
        } else { user = userService.createAnonimUser(); } // если токен == null или у него id == null, то создаем анонимного пользователя

        return user;
    }

    private Boolean isPassed(UserAnswers userAnswers) {
        List<Choice> choices = userAnswers.getUserAnswers();
        if (choices.size() != 45) return false;
        if(choices.stream().filter(choice -> choice.getArea() == Area.GOAL).count() != 15
                || choices.stream().filter(choice -> choice.getArea() == Area.QUALITY).count() != 15
                || choices.stream().filter(choice -> choice.getArea() == Area.STATE).count() != 15
                ) return false;

        if(choices.stream().anyMatch(choice -> choice.getChosenScale() == null)) return false;

        return true;
    }

//    @Override
//    @Transactional
//    public UserAnswers save(UserAnswers userAnswers){
////        Boolean isPresent = userAnswersRepository.findById(userAnswers.getId()).isPresent();
//        if(userAnswers.getId() != null) {
//            Optional<UserAnswers> optional = userAnswersRepository.findById(userAnswers.getId());
//            if(optional.isPresent()){
//                if(optional.get().getUserAnswers().size() >= 45) { // если userAnswers уже полностью пройден (45 вопросов), то записывать в новый userAnswers
//                    userAnswers.setId(null);
//                    userAnswers = insert(userAnswers); // userAnswers = новый тест
//                } else {
//                    userAnswers = update(userAnswers); // userAnswers уже есть в бд
//                }
//            }
//        }
//        else {
//            userAnswers = insert(userAnswers); // userAnswers нет в бд
//        }
//        return userAnswers;
//    }

    private UserAnswers insert(List<Choice> choices, User user) {
        canPassTestAgain(user);
        UserAnswers userAnswers = getInitUserAnswers();
        userAnswers.setUser(user);
        userAnswers.setUserAnswers(choices);
        userAnswers.setPassDate(LocalDateTime.now());
        userAnswers.setCreationDate(LocalDateTime.now());
        userAnswers.setPassed(isPassed(userAnswers));
        return userAnswersRepository.insert(userAnswers);
    }

    private UserAnswers update(UserAnswers userAnswers, List<Choice> choices) {

        // засетить юзера, если был аноним, а стал нормальным principal

        // UPDATE CHOICES
        Update updateUserAnswers = new Update().set("passDate", LocalDateTime.now()).push("userAnswers").each(choices);
        Query query = Query.query(Criteria.where(Fields.UNDERSCORE_ID).is(userAnswers.getId()));
        mongoOperations.updateFirst(query, updateUserAnswers, UserAnswers.class);

        userAnswers = userAnswersRepository.findById(userAnswers.getId())
                .orElseThrow(() -> new BadRequestException(env.getProperty("error.DataBaseError")));

        // UPDATE PASSED
        if (isPassed(userAnswers)) { // если тест пройден, то установить поле "passed" в UserAnswers = true
            mongoOperations.updateFirst(query, new Update().set("passed", true), UserAnswers.class);
        }
        userAnswers = userAnswersRepository.findById(userAnswers.getId())
                .orElseThrow(() -> new BadRequestException(env.getProperty("error.DataBaseError")));

        return userAnswers;
    }

    @Override
    public UserAnswers findById(ObjectId id) {
        return userAnswersRepository.findById(id)
                .orElseThrow(() -> new BadRequestException(env.getProperty("error.noUserAnswersFind")));
    }

    @Override
    public UserAnswers findLastUserAnswersByUserNameOrEmail(String userName) {
        if(userName == null) throw new BadRequestException(env.getProperty("error.noUserFind"));
        User user;
//       user = userRepository.findFirstByName(userName).orElseThrow(() -> new BadRequestException(env.getProperty("error.noUserFind")));
        if(userRepository.findUserByNameOrEmail(userName, userName).isPresent()) {
            user = userRepository.findUserByNameOrEmail(userName, userName).get();
        } else {
            user = new User();
            user.setName(userName);
        }

        UserAnswers userAnswer;
        Optional<List<UserAnswers>> userAnswers = userAnswersRepository.findAllByUser_IdOrderByIdDesc(user.getId());
        Boolean userAnswersIsPresent = !userAnswers.get().isEmpty();
        if(userAnswersIsPresent) {
            userAnswer = userAnswersRepository.findAllByUser_IdOrderByIdDesc(user.getId()).get().get(0);
        } else userAnswer = getInitUserAnswers();
        return userAnswer;
    }

    @Override
    public List<UserAnswers> findAllByUserNameOrderByCreationDateDesc(String userName) {
        if(userName == null) throw new BadRequestException(env.getProperty("error.noUserFind"));

        User user = userRepository.findFirstByName(userName)
                .orElseThrow(() -> new BadRequestException(env.getProperty("error.noUserFind")
                        + " for user name: " + userName));

        //TODO может возникнуть проблема, если в _id в UserAnswers ObjectId когда-нибудь повторится.
        // Вроде не должно, так как ObjectId  ("OrderById") отсортирован по дате создания
        // правда с точностью 1 сек. Плюс - не надо индекс на PassDate/CreationDate в UserAnswers создавать
        return userAnswersRepository.findAllByUser_IdOrderByIdDesc(user.getId())
                .orElseThrow(() ->new BadRequestException(env.getProperty("error.noUserAnswersFind")
                        + " for user with name: " + userName));
        // TODO вернуть null при exception?
    }

    @Override
    public UserAnswers getLastPassedTest() {
        User user = getUser(null);
        return userAnswersRepository.findTopByUser_IdAndPassedOrderByPassDateDesc(user.getId(),true)
                .orElseThrow(() -> new BadRequestException(env.getProperty("error.noPassedUserAnswersFind")));
    }

    @Override
    public UserAnswers getInitUserAnswers(){
        UserAnswers userAnswers = new UserAnswers();
        userAnswers.setUserAnswers(choiceList());
        return userAnswers;
    }

    @Override
    public Map<Scale, Double> getValueProfile() {
        User user = getUser(null);
        UserAnswers userAnswer = userAnswersRepository.findTopByUser_IdAndPassedOrderByPassDateDesc(user.getId(),true)
                .orElseThrow(() -> new BadRequestException(env.getProperty("error.noPassedUserAnswersFind")));
        Map<Scale, Double> valueProfile = new HashMap<>();
        final Integer totalNumberOfQuestions = Integer.valueOf(env.getProperty("total.number.of.questions"));

        userAnswer.getUserAnswers()
                .stream()
                .collect(groupingBy(choice -> choice.getChosenScale(), counting()))
                .forEach((scale, value) ->
                        valueProfile.put(scale, value.doubleValue()/totalNumberOfQuestions));

        // if user didn't choose some scale at all, than we have to put this scale (scales) to answer
        List<Scale> scales = getScales();
        scales.forEach(scale -> valueProfile.putIfAbsent(scale, 0d));

        //sort by scale in descending order
        Map<Scale, Double> sortedValueProfile = valueProfile.entrySet().stream()
                .sorted(comparingByKey(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        return sortedValueProfile;
    }

    private List<Scale> getScales() {
        List<Scale> scales = new ArrayList<>();
        scales.add(Scale.ONE);
        scales.add(Scale.TWO);
        scales.add(Scale.THREE);
        scales.add(Scale.FOUR);
        scales.add(Scale.FIVE);
        scales.add(Scale.SIX);
        return scales;
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

        //set random first and second test scale
        Boolean random = new Random().nextBoolean();
        Scale one = random ? scaleOne : scaleTwo;
        Scale two = random ? scaleTwo : scaleOne;

        choice.setFirstScale(one);
        choice.setSecondScale(two);

        return choice;
    }

    private User validateUserByUserName(User user){
        return userRepository.findFirstByName(user.getName()).orElseThrow(() ->
               new BadRequestException(env.getProperty("error.UserAlreadyExists") + "with user name: " + user.getName()));
    }

    private void validateChoices(List<Choice> choices, Area area) {
        if (choices.size() != 15) throw new BadRequestException(env.getProperty("error.ItMustBe15TestsForArea") + " " + area);
        if ( choices.stream().anyMatch(choice -> choice.getArea() != area) ) {
            throw new BadRequestException(env.getProperty("error.AreaMustBe") + " " + area);
        }
        if(choices.stream().anyMatch(choice -> choice.getChosenScale() == null))
            throw new BadRequestException(env.getProperty("error.ChosenScaleCan'tBeNull") + " for area: " + area);
    }

}
