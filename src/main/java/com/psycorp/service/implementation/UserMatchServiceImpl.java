package com.psycorp.service.implementation;

import com.psycorp.exception.BadRequestException;
import com.psycorp.model.entity.*;
import com.psycorp.model.enums.Area;
import com.psycorp.model.enums.MatchMethod;
import com.psycorp.repository.UserAnswersRepository;
import com.psycorp.repository.UserMatchRepository;
import com.psycorp.repository.UserRepository;
import com.psycorp.service.UserMatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@PropertySource("classpath:errormessages.properties")
public class UserMatchServiceImpl implements UserMatchService {

    private final UserMatchRepository userMatchRepository;

    private final UserRepository userRepository;

    private final UserAnswersRepository userAnswersRepository;

    private final Environment env;

    @Autowired
    public UserMatchServiceImpl(UserMatchRepository userMatchRepository, UserRepository userRepository,
                                UserAnswersRepository userAnswersRepository, Environment env) {
        this.userMatchRepository = userMatchRepository;
        this.userRepository = userRepository;
        this.userAnswersRepository = userAnswersRepository;
        this.env = env;
    }

    //TODO добавить проверку всех значений и соответствуюшии им Exceptions

    @Override
    public UserMatch insert(UserMatch userMatch) {
        return userMatchRepository.insert(userMatch);
    }

    @Override
    public List<UserMatch> findByUserName(String userName) {
        return userMatchRepository.findByUserName(userName);
    }

    @Override
    public List<UserMatch> findByMatchMethod(MatchMethod matchMethod) {
        return userMatchRepository.findByMatchMethod(matchMethod);
    }

    @Override
    public List<UserMatch> findByUserNameAndMatchMethod(String userName, MatchMethod matchMethod) {
        //TODO не получается через постмен сделать empty или null
        // (просто пропуск имени или метода(.../getAll//pearsoncorrelation) выдает 404)
        if(userName.isEmpty() || userName == null || matchMethod.name().isEmpty()) throw new BadRequestException("&&&&&&&&&&&&");
        return userMatchRepository.findByUserNameAndMatchMethod(userName, matchMethod);
    }

    @Override
    public List<UserMatch> getAll(){return userMatchRepository.findAll();}

    //TODO переделать метод так, чтобы расчитывал значения для всех методов
    @Override
    public UserMatch match(User user1, User user2, MatchMethod matchMethod){
        validate(user1);
        validate(user2);
        validateUserAnswers(user1);
        validateUserAnswers(user2);

        //последний сохраненный UserAnswers
//        UserAnswers userAnswers1 = userAnswersRepository.findAllByUser_IdOrderByPassDateDesc(user1.getId()).get(0);
//        UserAnswers userAnswers2 = userAnswersRepository.findAllByUser_IdOrderByPassDateDesc(user2.getId()).get(0);

//        UserAnswers userAnswers1 = userAnswersRepository.findAllByUser_NameOrderByPassDateDesc(user1.getName()).get(0);
//        UserAnswers userAnswers2 = userAnswersRepository.findAllByUser_NameOrderByPassDateDesc(user2.getName()).get(0);
//
        UserAnswers userAnswers1 = userAnswersRepository.findAllByUser_NameOrderByIdDesc(user1.getName()).get(0);
        UserAnswers userAnswers2 = userAnswersRepository.findAllByUser_NameOrderByIdDesc(user2.getName()).get(0);


        Set<Choice> choices1 = userAnswers1.getUserAnswers();
        Set<Choice> choices2 = userAnswers2.getUserAnswers();

       List<Match> matches = matchMap(choices1, choices2, matchMethod);

        Set<User> users = new HashSet<>();
        users.add(user1);
        users.add(user2);


//        Set<String> userNames = new HashSet<>();
//        userNames.add(user1.getName());
//        userNames.add(user2.getName());

        UserMatch userMatch = new UserMatch();
//        userMatch.setUserNames(userNames);
        userMatch.setUsers(users);

        userMatch.setMatches(matches);
//        userMatch.setMatchMethod(matchMethod);

        userMatch = userMatchRepository.insert(userMatch);
        return userMatch;
    }

    private List<Match> matchMap(Set<Choice> choices1, Set<Choice> choices2, MatchMethod matchMethod) {

       List<Match> matches = new ArrayList<>();

        for (Area area : Area.values()) {
            matches.add(getMatchForMatchMethod(choices1, choices2, matchMethod, area));
        }
//            matches.add(getMatchForMatchMethod(choices1, choices2, matchMethod, Area.GOAL));
//            matches.add(getMatchForMatchMethod(choices1, choices2, matchMethod, Area.QUALITY));
//            matches.add(getMatchForMatchMethod(choices1, choices2, matchMethod, Area.STATE));
//            matches.add(getMatchForMatchMethod(choices1, choices2, matchMethod, Area.TOTAL));

         return matches;
    }

    private Match getMatchForMatchMethod(Set<Choice> choices1, Set<Choice> choices2, MatchMethod matchMethod, Area area) {
        Match match = new Match();
        match.setArea(area);

        Result result = new Result();
//        Double result;
        if(matchMethod == MatchMethod.PERCENT){
//            result = areaMatchPercent(choices1, choices2, area);
            result.setNumber(areaMatchPercent(choices1, choices2, area));}

        else if(matchMethod == MatchMethod.PEARSONCORRELATION){
//            result = areaMatchPearson(choices1, choices2, area);
            result.setNumber(areaMatchPearson(choices1, choices2, area));
        } else throw new BadRequestException(env.getProperty("error.ThereIsn'tThatMatchMethod"));

        match.setResult(result);
        match.setMatchMethod(matchMethod);
        return match;
    }

    private Double areaMatchPearson(Set<Choice> choices1, Set<Choice> choices2, Area area){

        int totalSize = getTotalSize(area, choices1, choices2);

        double numOfMatches = (double) numbOfSecondScaleMatchesInTwoSets(choices1, choices2, area) / totalSize;
        double numOfSecondScaleMatchesForUser1 = (double) numOfSecondScaleMatches(choices1, area) / totalSize;
        double numOfSecondScaleMatchesForUser2 = (double) numOfSecondScaleMatches(choices2, area) / totalSize;

        Double pearsonCorrelationCoefficient = getPearsonCorrelationCoefficient(numOfMatches,
                numOfSecondScaleMatchesForUser1, numOfSecondScaleMatchesForUser2);

        return pearsonCorrelationCoefficient;
    }

    private Double getPearsonCorrelationCoefficient(double numberOfMatches, double numOfSecondScaleMatchesForUser1,
                                                    double numOfSecondScaleMatchesForUser2) {
        if(numberOfMatches == 1.0) return 1.0;

        //TODO что возвращать, если numOfSecondScaleMatchesForUser == 1.0?
        if(numOfSecondScaleMatchesForUser1 == 1.0 || numOfSecondScaleMatchesForUser2 == 1.0) return 1.0;
        if(numOfSecondScaleMatchesForUser1 == 0.0 && numOfSecondScaleMatchesForUser2 == 0.0) return 1.0;

        return (numberOfMatches - numOfSecondScaleMatchesForUser1 * numOfSecondScaleMatchesForUser2)
                    / (Math.sqrt(numOfSecondScaleMatchesForUser1 * numOfSecondScaleMatchesForUser2
                    * (1 - numOfSecondScaleMatchesForUser1) * (1 - numOfSecondScaleMatchesForUser2)));
    }

    /**
     * Count the number of choices for area, where chosenScale is secondScale for both choices1 and choices2
     * @param choices1
     * @param choices2
     * @param area
     * @return number of choices for area, where chosenScale is secondScale for both choices1 and choices2
     */
    private int numbOfSecondScaleMatchesInTwoSets(Set<Choice> choices1, Set<Choice> choices2, Area area) {

        if(area == null || area == Area.TOTAL) {
            return (int) choices1.stream()
                    .filter(choice -> (choice.getChosenScale() == choice.getSecondScale()
                            && choices2.contains(choice)) )
                    .count();
        }

        return (int) choices1.stream()
                .filter(choice -> (
                        choice.getArea().equals(area)
                                && choice.getChosenScale() == choice.getSecondScale()
                                && choices2.contains(choice)) )
                .count();
        }

    /**
     * Count the number of choices for area, where chosenScale is secondScale
     * @param choices
     * @param area
     * @return number of choices for area, where chosenScale is secondScale
     */
    private int numOfSecondScaleMatches(Set<Choice> choices, Area area) {

        if(area == null || area == Area.TOTAL) {
            return (int) choices.stream()
                    .filter(choice -> choice.getChosenScale() == choice.getSecondScale())
                    .count();
        }

        return (int) choices.stream()
                .filter(choice -> choice.getArea().equals(area)
                        && choice.getChosenScale() == choice.getSecondScale())
                .count();
    }

    /**
     * Calculate percent of matches in
     * @param choices1
     * @param choices2
     * @param area
     * @return
     */
    private Double areaMatchPercent(Set<Choice> choices1, Set<Choice> choices2, Area area){

        int totalSize = getTotalSize(area, choices1, choices2);

        int numbOfMatches = numbOfMatchesInTwoSets(choices1, choices2, area);
        Double MatchPercent = (double) 100 * numbOfMatches / totalSize;
        return MatchPercent;
    }

    /**
     * Get amount of choices in choices1 and choices2 that has area
     * @param area
     * @param choices1
     * @param choices2
     * @return amount of choices in choices1 and choices2 that has area;
     * or size of biggest from choices1 and choices if area = null
     */
    private int getTotalSize(Area area, Set<Choice> choices1, Set<Choice> choices2) {
        int totalSize1 = choices1.size();
        int totalSize2 = choices2.size();
        int totalSize;

        if(area == null || area == Area.TOTAL) {
            //TODO Как вариант, если не равны totalSize для choices1 и choices2, throw new BadRequestException(env.getProperty("error.ThereWrongTestResultForUser"))
            //Вопрос в том, что делать, если по какой-то причине в бд сохранено не полный тест
            // (ответы на 45 вопросов для Ценностно-смысловой совместимости), а только часть
            totalSize = (totalSize1 > totalSize2) ? totalSize1 : totalSize2;
        } else totalSize = (int) (choices2.stream().filter(choice -> choice.getArea().equals(area)).count());
        return totalSize;
    }

    /**
     * Count the number of choices for area, where chosenScale is equal for both choices1 and choices2
     * @param choices1
     * @param choices2
     * @param area
     * @return number of choices for area, where chosenScale is equal for both choices1 and choices2
     */
    private int numbOfMatchesInTwoSets(Set<Choice> choices1, Set<Choice> choices2, Area area) {

        if(area == null || area == Area.TOTAL) {
           return (int) choices1.stream()
                    .filter(choice -> (choices2.contains(choice)))
                    .count();
        }

       return (int) choices1.stream()
                .filter(choice -> (
                        choice.getArea().equals(area)
                                && choices2.contains(choice)) )
                .count();
    }

    private Boolean validate(User user){
        if (user == null || !userRepository.existsById(user.getName()))
            throw new BadRequestException(env.getProperty("error.noUserFind"));

        return true;
    }

    private Boolean validateUserAnswers(User user){

        if(userAnswersRepository.findAllByUser_NameOrderByIdDesc(user.getName()).get(0) == null)
            throw new BadRequestException(env.getProperty("error.noTestWasPassed"));

        return true;
    }

}
