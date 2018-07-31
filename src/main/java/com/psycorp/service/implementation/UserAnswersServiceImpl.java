package com.psycorp.service.implementation;

import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import com.psycorp.exception.BadRequestException;
import com.psycorp.model.entity.Choice;
import com.psycorp.model.entity.User;
import com.psycorp.model.entity.UserAnswers;
import com.psycorp.model.enums.Area;
import com.psycorp.model.enums.Scale;
import com.psycorp.repository.UserAnswersRepository;
import com.psycorp.repository.UserRepository;
import com.psycorp.service.UserAnswersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@PropertySource("classpath:errormessages.properties")
@PropertySource(value = {"classpath:scales/scalesrussian.properties"}, encoding = "utf-8", ignoreResourceNotFound = true)
public class UserAnswersServiceImpl implements UserAnswersService {

    private final UserAnswersRepository userAnswersRepository;

    private final UserRepository userRepository;

    private  final MongoOperations mongoOperations;

    private final Environment env;

    @Autowired
    public UserAnswersServiceImpl(UserAnswersRepository userAnswersRepository, UserRepository userRepository
            , MongoOperations mongoOperations, Environment env) {
        this.userAnswersRepository = userAnswersRepository;
        this.userRepository = userRepository;
        this.mongoOperations = mongoOperations;
        this.env = env;
    }

    //TODO добавить проверку всех значений и соответствуюшии им Exceptions
    @Override
    public UserAnswers insert(UserAnswers userAnswers
//            , Principal principal
    ){
        if(userAnswers.getId() != null && userAnswersRepository.findById(userAnswers.getId()).isPresent()){

//        Set<Choice> choices =  userAnswers.getUserAnswers();
//        Update update = new Update().push("userAnswers").each(choices);
//        update.push("userAnswers").each(choices);
//        push.each(choices);

        mongoOperations.updateFirst(Query.query(Criteria.where(Fields.UNDERSCORE_ID).is(userAnswers.getId()))
                , new Update().set("passDate", LocalDateTime.now()).push("userAnswers").each(userAnswers.getUserAnswers())
                , UserAnswers.class);

                return userAnswersRepository.findById(userAnswers.getId()).get();
        }
        else {
            userAnswers.setPassDate(LocalDateTime.now());
            userAnswers.setCreationDate(LocalDateTime.now());

            // если с principal все в порядке, то назначаем userAnswers этого залогиненого пользователя (даже, если в userAnswers передается какой-то другой пользователь)
//        if(principal != null && principal.getName() != null) {
            if (userRepository.findFirstByName(userAnswers.getUser().getName()) != null) {//заменить на то, что на верхней строчке
//            userAnswers.setUser(userRepository.findFirstByName(principal.getName()));

            } else {
                //если тестирование проходит еще не залогиненный пользователь, то на фронтенде обязываем его
                // заполнить поля "name" и 'email' и проверяем, есть ли такой уже в бд (на фротенде это тоже надо как-то делать)
                if (userAnswers.getUser() != null && userAnswers.getUser().getName() != null) {
                    if (userRepository.findFirstByName(userAnswers.getUser().getName()) != null) {
                        throw new BadRequestException(env.getProperty("error.UserAlreadyExists"));
                    }
                    //добавляем в бд нового пользователя (для не залогиненных пользователей)
                    userRepository.insert(userAnswers.getUser());
                } else throw new BadRequestException(env.getProperty("error.UserCan`tBeNull"));
            }

            return userAnswersRepository.insert(userAnswers);
        }
    }

    @Override
    public UserAnswers findLastByUserName(String userName) {
    return userAnswersRepository.findFirstByUser_NameOrderByIdDesc(userName);
    }

    @Override
    public List<UserAnswers> findAllByUserNameOrderByCreationDateDesc(String userName) {
        if(userName == null) throw new BadRequestException(env.getProperty("error.noUserFind"));

        User user = userRepository.findFirstByName(userName);
        if(user == null) throw new BadRequestException(env.getProperty("error.noUserFind"));

        //TODO может возникнуть проблема, если в _id в UserAnswers ObjectId когда-нибудь повторится.
        // Вроде не должно, так как ObjectId  ("OrderById") отсортирован по дате создания
        // правда с точностью 1 сек. Плюс - не надо индекс на PassDate/CreationDate в UserAnswers создавать
        return userAnswersRepository.findAllByUser_NameOrderByIdDesc(user.getName());
    }

    @Override
    public UserAnswers getInitUserAnswers(){
        UserAnswers userAnswers = new UserAnswers();
        userAnswers.setUserAnswers(choiceList());
        return userAnswers;
    }

    @Override
    public List<Choice> choiceList(){

        //TODO переделять на Map<String, List<ChoiceDto>>, где ключ - goal, quality, state

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
//TODO разабраться с id
        Choice choice = new Choice();
        choice.setArea(area);
        choice.setFirstScale(scaleOne);
        choice.setSecondScale(scaleTwo);
//        choice.setId(new ObjectId());
        return choice;
    }
}
