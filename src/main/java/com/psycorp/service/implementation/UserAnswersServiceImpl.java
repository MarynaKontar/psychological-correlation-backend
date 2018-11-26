package com.psycorp.service.implementation;

import com.psycorp.exception.AuthorizationException;
import com.psycorp.exception.BadRequestException;
import com.psycorp.model.entity.*;
import com.psycorp.model.enums.Area;
import com.psycorp.model.enums.ErrorEnum;
import com.psycorp.model.enums.Scale;
import com.psycorp.repository.UserAnswersRepository;
import com.psycorp.repository.UserRepository;
import com.psycorp.security.token.TokenPrincipal;
import com.psycorp.service.UserAnswersService;
import com.psycorp.service.UserService;
import com.psycorp.service.security.AuthService;
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
import java.util.*;

import static com.psycorp.security.SecurityConstant.ACCESS_TOKEN_PREFIX;

@Service
public class UserAnswersServiceImpl implements UserAnswersService {

    private final UserAnswersRepository userAnswersRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final UserService userService;
    private final MongoOperations mongoOperations;
    private final Environment env;

    @Autowired
    public UserAnswersServiceImpl(UserAnswersRepository userAnswersRepository, UserRepository userRepository,
                                  AuthService authService, UserService userService, MongoOperations mongoOperations,
                                  Environment env) {
        this.userAnswersRepository = userAnswersRepository;
        this.userRepository = userRepository;
        this.authService = authService;
        this.userService = userService;
        this.mongoOperations = mongoOperations;
        this.env = env;
    }

    @Override
    @Transactional
    public UserAnswersEntity saveChoices(String token, UserAnswersEntity userAnswersEntity, List<Choice> choices, Area area){

        validateChoices(choices, area);

        //GET USER
        User user = getUserByToken(token);
//        Optional<UserAnswersEntity> userAnswersOptional = userAnswersRepository.findTopByUser_IdAndPassedOrderByPassDateDesc(user.getId(), false);
//        Optional<UserAnswersEntity> userAnswersOptional1 =userAnswersRepository.findTopByUser_IdAndPassedOrderByPassDateDesc(user.getId(), false)
//                .map((ua) -> update(ua, choices));
        userAnswersEntity = userAnswersRepository.findTopByUser_IdAndPassedOrderByPassDateDesc(user.getId(), false)
                .map((ua) -> update(ua, choices))
                .orElseGet(() -> insert(choices, user));
//        userAnswersEntity = userAnswersRepository.findAllByUser_IdAndPassedAndLastPassDate(user.getId(), false)
//                .map((ua) -> update(ua, choices))
//                .orElse(insert(choices, user));

        //UPDATE
        // если есть userAnswersEntity у user, то вытянем его по userId и сохраним в него choices (если не isPassed)
        // или сохраним choices в новый UserAnswersEntity (если isPassed)
//        if(!userAnswersRepository.findAllByUser_IdAndPassedOrderByPassDateDesc(user.getId(), false).isEmpty()) {
//            List<UserAnswersEntity> userAnswersList = userAnswersRepository.findAllByUser_IdAndPassedOrderByPassDateDesc(user.getId(), false);
//            userAnswersEntity = userAnswersList.get(0);
//
//            //проверяем, совпадают ли пользователь у userAnswersEntity с user из principal, если нет - exception
//            //для нового юзера не пройдет - убрать
////            userAuthorization(userAnswersEntity.getUser().getId());
//
//            userAnswersEntity = update(userAnswersEntity, choices);
//
//
//        //INSERT
//        // если последний тест уже пройден и прошел срок, после которого можно повторно пройти тест (если нет - exception)
//        } else {
//            userAnswersEntity = insert(choices, user);
//            //что если прошел только часть тестов (goal, например), а потом заново начал через день?
//        }





//        //UPDATE
//        // если есть id у userAnswersEntity, то даже если с фронта пришел какой-то не тот userAnswersEntity, но с такой id,
//        // то вытянем по id и сохраним в него choices (если не isPassed) или сохраним choices в новый UserAnswersEntity (если isPassed)
//        if(userAnswersEntity.getId() != null && userAnswersRepository.findById(userAnswersEntity.getId()).isPresent()) {
//            userAnswersEntity = userAnswersRepository.findById(userAnswersEntity.getId()).get(); // находим по id
//
//            if(!userAnswersRepository.findAllByUser_IdAndPassedOrderByPassDateDesc(user.getId(), false).isEmpty()) {
//                List<UserAnswersEntity> userAnswersList = userAnswersRepository.findAllByUser_IdAndPassedOrderByPassDateDesc(user.getId(), false);
//                userAnswersEntity = userAnswersList.get(0);
//            }
//            //проверяем, совпадают ли пользователь у userAnswersEntity с user из principal, если нет - exception
//            userAuthorization(userAnswersEntity.getUser().getId());
//
//            //если последний тест уже пройден (заменить после рефакторинга др. методов на userAnswersEntity.getPassed)
//            // и прошел ли срок, после которого можно повторно пройти тест (если нет - exception) isCanPassTest
//            if(isPassed(userAnswersEntity)) {
//                canPassTestAgain(user);
//                userAnswersEntity = insert(choices, user);
//            }
//            userAnswersEntity = update(userAnswersEntity, choices);
//
//
//            //INSERT
//        }

        return userAnswersEntity;
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
//            Optional<List<UserAnswersEntity>> userAnswersOptional = userAnswersRepository.findAllByUser_IdOrderByIdDesc(user.getId());
            Optional<UserAnswersEntity> userAnswersOptional1 = userAnswersRepository.findTopByUser_IdOrderByPassDateDesc(user.getId());

            if(userAnswersOptional1.isPresent()){
                UserAnswersEntity userAnswersEntity = userAnswersOptional1.get();
            }

//            if (userAnswersOptional.isPresent() && userAnswersOptional.get().isEmpty()) {
//                UserAnswersEntity userAnswers = userAnswersOptional.get().get(0);
//           if(Period.between(userAnswers.getPassDate().toLocalDate(), LocalDateTime.now().toLocalDate()).getDays()
//                   <= Period.ofMonths(6).getDays()) throw new BadRequestException(env.getProperty("error.YouCan'tPassNewTest"));
        }
    }

    private User getUserByToken(String token) {

        // если пользователь заходит на сайт по ссылке с токеном, то ищем пользователя по этому токену
        if(token != null && !token.isEmpty()) {
           return authService.getUserByToken(token.substring(ACCESS_TOKEN_PREFIX.length() + 1));
        }

        User user = getPrincipal();

        return user;
    }

    private User getPrincipal() {

        User principal;
        TokenPrincipal tokenPrincipal = (TokenPrincipal) authService.getAuthPrincipal();

        if(tokenPrincipal != null && tokenPrincipal.getId() != null) { //если есть токен
//            if(userRepository.findById(tokenPrincipal.getId()).isPresent()){ // и для него есть пользователь, то берем этого пользователя
//                principal = userRepository.findById(tokenPrincipal.getId()).get();
//            } else { principal = userService.createAnonimUser(); } // если для этого токена нет пользователя, то создаем анонимного (наверное надо кидать ошибку. Это случай, когда в бд не правильно сохраняли)
            principal = userService.findById(tokenPrincipal.getId());
        } else { principal = userService.createAnonimUser(); } // если токен == null или у него id == null, то создаем анонимного пользователя

        return principal;
    }

    private Boolean isPassed(UserAnswersEntity userAnswersEntity) {
        Integer numberOfQuestions = Integer.valueOf(env.getProperty("total.number.of.questions"));
        List<Choice> choices = userAnswersEntity.getUserAnswers();
        if (choices.size() != 45) return false;
        if(choices.stream().filter(choice -> choice.getArea() == Area.GOAL).count() != numberOfQuestions
                || choices.stream().filter(choice -> choice.getArea() == Area.QUALITY).count() != numberOfQuestions
                || choices.stream().filter(choice -> choice.getArea() == Area.STATE).count() != numberOfQuestions
                ) return false;

        if(choices.stream().anyMatch(choice -> choice.getChosenScale() == null)) return false;

        return true;
    }

//    @Override
//    @Transactional
//    public UserAnswersEntity save(UserAnswersEntity userAnswers){
////        Boolean isPresent = userAnswersRepository.findById(userAnswers.getId()).isPresent();
//        if(userAnswers.getId() != null) {
//            Optional<UserAnswersEntity> optional = userAnswersRepository.findById(userAnswers.getId());
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

    private UserAnswersEntity insert(List<Choice> choices, User user) {
        canPassTestAgain(user);
        UserAnswersEntity userAnswersEntity = getInitUserAnswers();
        userAnswersEntity.setUser(user);
        userAnswersEntity.setUserAnswers(choices);
        userAnswersEntity.setPassDate(LocalDateTime.now());
        userAnswersEntity.setCreationDate(LocalDateTime.now());
        userAnswersEntity.setPassed(isPassed(userAnswersEntity));
        return userAnswersRepository.insert(userAnswersEntity);
    }

    private UserAnswersEntity update(UserAnswersEntity userAnswersEntity, List<Choice> choices) {

        // засетить юзера, если был аноним, а стал нормальным principal

        // UPDATE CHOICES
        Update updateUserAnswers = new Update().set("passDate", LocalDateTime.now()).push("userAnswers").each(choices);
        Query query = Query.query(Criteria.where(Fields.UNDERSCORE_ID).is(userAnswersEntity.getId()));
        mongoOperations.updateFirst(query, updateUserAnswers, UserAnswersEntity.class);

        userAnswersEntity = userAnswersRepository.findById(userAnswersEntity.getId())
                .orElseThrow(() -> new BadRequestException(env.getProperty("error.DataBaseError")));

        // UPDATE PASSED
        if (isPassed(userAnswersEntity)) { // если тест пройден, то установить поле "passed" в UserAnswersEntity = true
            mongoOperations.updateFirst(query, new Update().set("passed", true), UserAnswersEntity.class);
        }
        userAnswersEntity = userAnswersRepository.findById(userAnswersEntity.getId())
                .orElseThrow(() -> new BadRequestException(env.getProperty("error.DataBaseError")));

        return userAnswersEntity;
    }

    @Override
    public UserAnswersEntity findById(ObjectId id) {
        return userAnswersRepository.findById(id)
                .orElseThrow(() -> new BadRequestException(env.getProperty("error.noUserAnswersFind")));
    }

    @Override
    public UserAnswersEntity findLastUserAnswersByUserNameOrEmail(String userName) {
        if(userName == null) throw new BadRequestException(env.getProperty("error.noUserFound"));
        User user;
//       user = userRepository.findFirstByName(userName).orElseThrow(() -> new BadRequestException(env.getProperty("error.noUserFind")));
        if(userRepository.findUserByNameOrEmail(userName, userName).isPresent()) {
            user = userRepository.findUserByNameOrEmail(userName, userName).get();
        } else {
            user = new User();
            user.setName(userName);
        }

        UserAnswersEntity userAnswer;
        Optional<List<UserAnswersEntity>> userAnswers = userAnswersRepository.findAllByUser_IdOrderByIdDesc(user.getId());
        Boolean userAnswersIsPresent = !userAnswers.get().isEmpty();
        if(userAnswersIsPresent) {
            userAnswer = userAnswersRepository.findAllByUser_IdOrderByIdDesc(user.getId()).get().get(0);
        } else userAnswer = getInitUserAnswers();
        return userAnswer;
    }

    @Override
    public List<UserAnswersEntity> findAllByUserNameOrderByCreationDateDesc(String userName) {
        if(userName == null) throw new BadRequestException(env.getProperty("error.noUserFound"));

        User user = userRepository.findFirstByName(userName)
                .orElseThrow(() -> new BadRequestException(env.getProperty("error.noUserFound")
                        + " for user name: " + userName));

        //TODO может возникнуть проблема, если в _id в UserAnswersEntity ObjectId когда-нибудь повторится.
        // Вроде не должно, так как ObjectId  ("OrderById") отсортирован по дате создания
        // правда с точностью 1 сек. Плюс - не надо индекс на PassDate/CreationDate в UserAnswersEntity создавать
        return userAnswersRepository.findAllByUser_IdOrderByIdDesc(user.getId())
                .orElseThrow(() ->new BadRequestException(env.getProperty("error.noUserAnswersFind")
                        + " for user with name: " + userName));
        // TODO вернуть null при exception?
    }

    @Override
    public UserAnswersEntity getLastPassedTest() {
        User user = getPrincipal();
        return userAnswersRepository.findTopByUser_IdAndPassedOrderByPassDateDesc(user.getId(),true)
                .orElseThrow(() -> new BadRequestException(env.getProperty("error.noPassedUserAnswersFind")));
    }

    @Override
    public UserAnswersEntity getLastPassedTest(User user) {
        if(user != null && user.getId() != null) {
            return userAnswersRepository.findTopByUser_IdAndPassedOrderByPassDateDesc(user.getId(), true)
                    .orElseThrow(() -> new BadRequestException(env.getProperty("error.noPassedUserAnswersFind")));
        } else throw new BadRequestException(env.getProperty("error.UserOrUserIdCan`tBeNull"));
    }

    @Override
    public UserAnswersEntity getInitUserAnswers(){
        UserAnswersEntity userAnswersEntity = new UserAnswersEntity();
        userAnswersEntity.setUserAnswers(choiceList());
        return userAnswersEntity;
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
