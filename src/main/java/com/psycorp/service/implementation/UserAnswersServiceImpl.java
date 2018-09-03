package com.psycorp.service.implementation;

import com.psycorp.exception.BadRequestException;
import com.psycorp.model.entity.Choice;
import com.psycorp.model.entity.User;
import com.psycorp.model.entity.UserAnswers;
import com.psycorp.model.enums.Area;
import com.psycorp.model.enums.Scale;
import com.psycorp.repository.UserAnswersRepository;
import com.psycorp.repository.UserRepository;
import com.psycorp.service.UserAnswersService;
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

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UserAnswersServiceImpl implements UserAnswersService {
    @Autowired
    private UserAnswersRepository userAnswersRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MongoOperations mongoOperations;
    @Autowired
    private Environment env;


    @Override
    public UserAnswers saveChoices(List<Choice> choices, Principal principal, String userName){
        //        User user = userRepository.findFirstByName(userName).orElse(null);
        // TODO добавить логику для 1-го,  второго... прохождения теста
        UserAnswers userAnswers = findLastUserAnswersByUserName(userName); // если не находит, то возвращает initUserAnswers
//        userAnswers.setUser(user);

        userAnswers.setUserAnswers(choices);
        return save(userAnswers);
    }

    @Override
    @Transactional
    public UserAnswers save(UserAnswers userAnswers){
//        Boolean isPresent = userAnswersRepository.findById(userAnswers.getId()).isPresent();
        if(userAnswers.getId() != null) {
            Optional<UserAnswers> optional = userAnswersRepository.findById(userAnswers.getId());
            if(optional.isPresent()){
                if(optional.get().getUserAnswers().size() >= 45) { // если userAnswers уже полностью пройден (45 вопросов), то записывать в новый userAnswers
                    userAnswers.setId(null);
                    userAnswers = insert(userAnswers); // userAnswers = новый тест
                } else {
                    userAnswers = update(userAnswers); // userAnswers уже есть в бд
                }
            }
        }
        else {
            userAnswers = insert(userAnswers); // userAnswers нет в бд
        }
        return userAnswers;
    }

    private UserAnswers insert(UserAnswers userAnswers) {
        // userAnswers нет в бд => инициируем даты и находим/записываем в бд user
        userAnswers.setPassDate(LocalDateTime.now());
        userAnswers.setCreationDate(LocalDateTime.now());

        if(userAnswers.getUser() == null) { // в userAnswers нет user
            //TODO что делать, если пользователь еще не зарегестрирован
//            throw new BadRequestException(env.getProperty("error.UserCan`tBeNull") + " You have to register");
            // сохраняем в бд без пользователя, отсылаем куда-то ссылку по которой можно потом найти эти результаты и зарегестрироваться

        } else { // в userAnswers есть user (зарегестрированный или нет)

            if ( userRepository.findById(userAnswers.getUser().getId()).isPresent() ) { // пользователь зарегестрирован
                // если с principal все в порядке, то назначаем userAnswers этого залогиненого пользователя (даже, если в userAnswers передается какой-то другой пользователь)
//                if(principal != null && principal.getName() != null) {
//                userAnswers.setUser(userRepository.findFirstByName(principal.getName()));

            } else {                                                                    // пользователь не зарегестрирован
                User user = validateUserByUserName(userAnswers.getUser());
                //добавляем в бд нового пользователя (для незарегестрированных пользователей)
                user = userRepository.insert(user);
                userAnswers.setUser(user);
            }
        }

        return userAnswersRepository.insert(userAnswers);
    }

    private UserAnswers update(UserAnswers userAnswers) {
        List<Choice> choices =  userAnswers.getUserAnswers();
        Update updateUser = new Update().set("passDate", LocalDateTime.now()).push("userAnswers").each(choices);
        Query query = Query.query(Criteria.where(Fields.UNDERSCORE_ID).is(userAnswers.getId()));
        mongoOperations.updateFirst(query, updateUser, UserAnswers.class);

        return userAnswersRepository.findById(userAnswers.getId()).get();
    }

    @Override
    public UserAnswers findById(ObjectId id) {
        return userAnswersRepository.findById(id)
                .orElseThrow(() -> new BadRequestException(env.getProperty("error.noUserAnswersFind")));
    }

    @Override
    public UserAnswers findLastUserAnswersByUserName(String userName) {
        if(userName == null) throw new BadRequestException(env.getProperty("error.noUserFind"));
        User user;
//       user = userRepository.findFirstByName(userName).orElseThrow(() -> new BadRequestException(env.getProperty("error.noUserFind")));
        if(userRepository.findFirstByName(userName).isPresent()) {
            user = userRepository.findFirstByName(userName).get();
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
    public UserAnswers getInitUserAnswers(){
        UserAnswers userAnswers = new UserAnswers();
        userAnswers.setUserAnswers(choiceList());
        return userAnswers;
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
        choice.setFirstScale(scaleOne);
        choice.setSecondScale(scaleTwo);
        return choice;
    }

    private User validateUserByUserName(User user){
        return userRepository.findFirstByName(user.getName()).orElseThrow(() ->
               new BadRequestException(env.getProperty("error.UserAlreadyExists") + "with user name: " + user.getName()));
    }

    @Override
    public void validateArea(List<Choice> choices, Area area) {
        if (choices.size() != 15) throw new BadRequestException(env.getProperty("error.ItMustBe15TestsForArea") + " " + area);
        if ( choices.stream().anyMatch(choice -> choice.getArea() != area) ) {
            throw new BadRequestException(env.getProperty("error.AreaMustBe") + " " + area);
        }
    }

}
