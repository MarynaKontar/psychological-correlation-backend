package com.psycorp.service.implementation;

import com.psycorp.exception.BadRequestException;
import com.psycorp.model.entity.Choice;
import com.psycorp.model.entity.User;
import com.psycorp.model.entity.UserAnswers;
import com.psycorp.model.entity.UserMatch;
import com.psycorp.model.enums.Area;
import com.psycorp.repository.UserAnswersRepository;
import com.psycorp.repository.UserMatchRepository;
import com.psycorp.repository.UserRepository;
import com.psycorp.service.UserMatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@PropertySource("classpath:errormessages.properties")
public class UserMatchServiceImpl implements UserMatchService {

    @Autowired
    private UserMatchRepository userMatchRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAnswersRepository userAnswersRepository;

    @Value("${error.noUserFind}")
    private String noUserFind;

    //TODO добавить проверку всех значений и соответствуюшии им Exceptions

    @Override
    public UserMatch insert(UserMatch userMatch) {
        return userMatchRepository.insert(userMatch);
    }


    //TODO Продумать архитектуру так, чтобы удобно было добавлять другие способы вычисления goalMatch, totalMatch,  qualityMatch, stateMatch
    @Override
    public String goalMatch(User user1, User user2) {
//TODO вынести в отдельный метод
        if (user1 == null || user2 == null
                || !userRepository.existsById(user1.getId()) || !userRepository.existsById(user2.getId())) {
            throw new BadRequestException(noUserFind);
        }

        UserAnswers userAnswers1 = userAnswersRepository.findByUser_Id(user1.getId());
        UserAnswers userAnswers2 = userAnswersRepository.findByUser_Id(user2.getId());

        Set<Choice> choices1 =  userAnswers1.getUserAnswers();
        Set<Choice> choices2 =  userAnswers2.getUserAnswers();

        choices1.removeIf(choice -> !choice.getArea().equals(Area.GOAL));
        choices2.removeIf(choice -> !choice.getArea().equals(Area.GOAL));

        choices1.removeAll(choices2);
        double totalMatchPercentInt = 100 * choices1.size()/choices2.size();

        return String.valueOf(totalMatchPercentInt);
    }


    @Override
    public String totalMatch(User user1, User user2) {

        if (user1 == null || user2 == null
                || !userRepository.existsById(user1.getId()) || !userRepository.existsById(user2.getId())) {
            throw new BadRequestException(noUserFind);
        }

        UserAnswers userAnswers1 = userAnswersRepository.findByUser_Id(user1.getId());
        UserAnswers userAnswers2 = userAnswersRepository.findByUser_Id(user2.getId());

        Set<Choice> choices1 =  userAnswers1.getUserAnswers();
        Set<Choice> choices2 =  userAnswers2.getUserAnswers();

        choices1.removeAll(choices2);
        double totalMatchPercentInt = 100 * choices1.size()/choices2.size();

        return String.valueOf(totalMatchPercentInt);

    }

    @Override
    public String qualityMatch(User user1, User user2) {
        if (user1 == null || user2 == null
                || !userRepository.existsById(user1.getId()) || !userRepository.existsById(user2.getId())) {
            throw new BadRequestException(noUserFind);
        }

        UserAnswers userAnswers1 = userAnswersRepository.findByUser_Id(user1.getId());
        UserAnswers userAnswers2 = userAnswersRepository.findByUser_Id(user2.getId());

        Set<Choice> choices1 =  userAnswers1.getUserAnswers();
        Set<Choice> choices2 =  userAnswers2.getUserAnswers();

        choices1.removeIf(choice -> !choice.getArea().equals(Area.QUALITY));
        choices2.removeIf(choice -> !choice.getArea().equals(Area.QUALITY));

        choices1.removeAll(choices2);
        double totalMatchPercentInt = 100 * choices1.size()/choices2.size();

        return String.valueOf(totalMatchPercentInt);

    }

    @Override
    public String stateMatch(User user1, User user2) {
        if (user1 == null || user2 == null
                || !userRepository.existsById(user1.getId()) || !userRepository.existsById(user2.getId())) {
            throw new BadRequestException(noUserFind);
        }

        UserAnswers userAnswers1 = userAnswersRepository.findByUser_Id(user1.getId());
        UserAnswers userAnswers2 = userAnswersRepository.findByUser_Id(user2.getId());

        Set<Choice> choices1 =  userAnswers1.getUserAnswers();
        Set<Choice> choices2 =  userAnswers2.getUserAnswers();

        choices1.removeIf(choice -> !choice.getArea().equals(Area.STATE));
        choices2.removeIf(choice -> !choice.getArea().equals(Area.STATE));

        choices1.removeAll(choices2);
        double totalMatchPercentInt = 100 * choices1.size()/choices2.size();

        return String.valueOf(totalMatchPercentInt);
        }
}
