package com.psycorp.service.implementation;

import com.psycorp.exception.BadRequestException;
import com.psycorp.model.entity.*;
import com.psycorp.model.enums.Area;
import com.psycorp.model.enums.MatchMethod;
import com.psycorp.model.objects.Matching;
import com.psycorp.model.objects.UserMatch;
import com.psycorp.repository.ValueCompatibilityAnswersRepository;
import com.psycorp.repository.UserMatchRepository;
import com.psycorp.service.UserMatchService;
import com.psycorp.service.UserService;
import com.psycorp.util.UserMatchCommentUtil;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

@Service
//@PropertySource("classpath:errormessages.properties")
public class UserMatchServiceImpl implements UserMatchService {

    private final UserMatchRepository userMatchRepository;
    private final UserService userService;
    private final ValueCompatibilityAnswersRepository valueCompatibilityAnswersRepository;

    private final Environment env;

    @Autowired
    public UserMatchServiceImpl(UserMatchRepository userMatchRepository, UserService userService,
                                ValueCompatibilityAnswersRepository valueCompatibilityAnswersRepository, Environment env) {
        this.userMatchRepository = userMatchRepository;
        this.userService = userService;
        this.valueCompatibilityAnswersRepository = valueCompatibilityAnswersRepository;
        this.env = env;
    }

    @Override
    public UserMatchEntity insert(UserMatchEntity userMatchEntity) {
        return userMatchRepository.insert(userMatchEntity);
    }

    @Override
    public List<UserMatchEntity> findByMatchMethod(MatchMethod matchMethod) {
        return userMatchRepository.findByMatchMethod(matchMethod);
    }

    @Override
    public List<UserMatch> getAll(){
        List<UserMatchEntity> matchEntities = userMatchRepository.findAll();
        List<UserMatch> matches = new ArrayList<>();
        matchEntities.forEach(matchEntity -> matches.add(getUserMatch(matchEntity)));
        return matches;
    }

    @Override
    public UserMatch match(User user1, User user2, MatchMethod matchMethod){

        User fullUser1 = userService.find(user1);
        User fullUser2 = userService.find(user2);
        validIfUsersCanBeMatching(fullUser1, fullUser2);

        ValueCompatibilityAnswersEntity valueCompatibilityAnswersEntity1 = getValueCompatibilityAnswers(fullUser1).get(0);
        ValueCompatibilityAnswersEntity valueCompatibilityAnswersEntity2 = getValueCompatibilityAnswers(fullUser2).get(0);

        List<UserMatchEntity> userMatchEntitiesUser1 = userMatchRepository.findByUsersIdContaining(fullUser1.getId());
        List<UserMatchEntity> userMatchEntitiesUser2 = userMatchRepository.findByUsersIdContaining(fullUser2.getId());

        // check if there is record in userMatchEntity collection that is made after of both valueCompatibilityAnswers and get it; if not - insert it
        UserMatchEntity userMatchEntity = userMatchEntitiesUser1.stream()
                .filter(userMatchPrincipal ->
                        userMatchEntitiesUser2.contains(userMatchPrincipal)
                                && userMatchPrincipal.getId().getDate().after(Timestamp.valueOf(valueCompatibilityAnswersEntity1.getPassDate()))
                                && userMatchPrincipal.getId().getDate().after(Timestamp.valueOf(valueCompatibilityAnswersEntity2.getPassDate())))
                .sorted(Comparator.comparing(UserMatchEntity::getId).reversed()).limit(1).findFirst()
                .orElseGet(() ->
                        insert(valueCompatibilityAnswersEntity1, valueCompatibilityAnswersEntity2, fullUser1, fullUser2, matchMethod)
                );

        return getUserMatch(userMatchEntity);
    }

    // TODO какой-то глюк: principal.getUsersForMatchingId().get(0) дает user только с одним элементом в usersForMatchingId, поэтому приходится брать через репозиторий
    @Override
    @Transactional
    public UserMatch match(User user, MatchMethod matchMethod){
        User principal = userService.getPrincipalUser();

        User user2;
        if (user != null) { // if get user from controller, than get it from db;
            user2 = userService.find(user);
            //TODO может не быть еще аккаунта у user (тестируются на одном компьютере; нельзя определить OPEN или нет) и надо определить как проверять, могут ли сравниваться пользователи
            if (user2.getUsersForMatchingId() == null
                    || user2.getUsersForMatchingId().isEmpty()
                    || !user2.getUsersForMatchingId().contains(principal.getId())) {
                user2 = userService.addNewUsersForMatching(user2, Collections.singletonList(principal), Update.Position.LAST);
            }
            if (principal.getUsersForMatchingId() == null
                    || principal.getUsersForMatchingId().isEmpty()
                    || !principal.getUsersForMatchingId().contains(user2.getId())) {
               principal = userService.addNewUsersForMatching(principal, Collections.singletonList(user2), Update.Position.LAST);
            }
        } else { //if not (user firstly tests and matching with user, that sent him token) - get first user from userForMatching list
            if(!principal.getUsersForMatchingId().isEmpty()) {
                    user2 = userService.findById(principal.getUsersForMatchingId().get(0));
            } else throw new BadRequestException(env.getProperty("error.noUserFound")
                    + " for matching with id: " + principal.getUsersForMatchingId().get(0));
        }

        validIfUsersCanBeMatching(principal, user2);

        //последний сохраненный ValueCompatibilityAnswersEntity
        ValueCompatibilityAnswersEntity valueCompatibilityAnswersEntity1 = getValueCompatibilityAnswers(principal).get(0);
        ValueCompatibilityAnswersEntity valueCompatibilityAnswersEntity2 = getValueCompatibilityAnswers(user2).get(0);

        List<UserMatchEntity> userMatchEntitiesPrincipal = userMatchRepository.findByUsersIdContaining(principal.getId());
        List<UserMatchEntity> userMatchEntitiesUser2 = userMatchRepository.findByUsersIdContaining(user2.getId());

        // check if there is record in userMatchEntity collection that is after of both valueCompatibilityAnswers; if not - insert it
        User finalPrincipal = principal;
        User finalUser = user2;
        UserMatchEntity userMatchEntity = userMatchEntitiesPrincipal.stream()
                .filter(userMatchPrincipal ->
                    userMatchEntitiesUser2.contains(userMatchPrincipal)
                    && userMatchPrincipal.getId().getDate().after(Timestamp.valueOf(valueCompatibilityAnswersEntity1.getPassDate()))
                    && userMatchPrincipal.getId().getDate().after(Timestamp.valueOf(valueCompatibilityAnswersEntity2.getPassDate())))
                .sorted(Comparator.comparing(UserMatchEntity::getId).reversed()).limit(1).findFirst()
                .orElseGet(() ->
                        insert(valueCompatibilityAnswersEntity1, valueCompatibilityAnswersEntity2, finalPrincipal, finalUser, matchMethod)
                );
        return getUserMatch(userMatchEntity);
    }

    private UserMatch getUserMatch(UserMatchEntity userMatchEntity) {
        List<Matching> matches = new ArrayList<>();
        userMatchEntity.getMatches().forEach(matchEntity -> matches.add(new Matching(matchEntity.getMatchMethod(),
                matchEntity.getArea(), matchEntity.getResult(), UserMatchCommentUtil.getAspectComment(matchEntity, env))));

        UserMatch userMatch = new UserMatch();
        userMatch.setId(userMatchEntity.getId());
        userMatch.setMatches(matches);

        userMatch.setUsers(getUsers(userMatchEntity.getUsersId()));
        return userMatch;
    }

    private Set<User> getUsers(Set<ObjectId> usersId) {
        return usersId
                .stream()
                .map(userService::findById)
                .collect(Collectors.toSet());
    }

    private void validIfUsersCanBeMatching(User user1, User user2) {
        if (user1.getUsersForMatchingId() == null || user2.getUsersForMatchingId() == null) {
            throw new BadRequestException(env.getProperty("error.UsersCan`tBeMatching"));
        }
        if (user1.getUsersForMatchingId() != null && user2.getUsersForMatchingId() != null) {
//            List<ObjectId> ids1 = user1.getUsersForMatchingId().stream().map(User::getId).collect(Collectors.toList());
            List<ObjectId> ids1 = new ArrayList<>(user1.getUsersForMatchingId());
            List<ObjectId> ids2 = new ArrayList<>(user2.getUsersForMatchingId());

            //TODO пропустит только если у user1 в  UsersForMatching есть user2 и наоборот. В дальнейшем добавить, чтобы пропускало, если "открытые" профили
            if (!ids1.contains(user2.getId()) || !ids2.contains(user1.getId())) {
                throw new BadRequestException(env.getProperty("error.UsersCan`tBeMatching"));
            }
        }

        // TODO если напрямую проверять содержат ли UsersForMatching user, то не получается (то ли зацикливание, то еще что)
//        if (user1.getUsersForMatchingId() == null || user2.getUsersForMatchingId() == null
//                || !user1.getUsersForMatchingId().contains(user2)
//                || !user2.getUsersForMatchingId().contains(user1)) {
//            throw new BadRequestException(env.getProperty("error.UsersCan`tBeMatching"));
//        }
    }

    private UserMatchEntity insert(ValueCompatibilityAnswersEntity valueCompatibilityAnswersEntity1,
                                   ValueCompatibilityAnswersEntity valueCompatibilityAnswersEntity2,
                                   User principal, User user,
                                   MatchMethod matchMethod) {
        List<Choice> choices1 = valueCompatibilityAnswersEntity1.getUserAnswers();
        List<Choice> choices2 = valueCompatibilityAnswersEntity2.getUserAnswers();

        List<MatchEntity> matches = matchMap(choices1, choices2, matchMethod);

        Set<ObjectId> usersId = new HashSet<>();
        usersId.add(principal.getId());
        usersId.add(user.getId());

        UserMatchEntity userMatchEntity = new UserMatchEntity();
        userMatchEntity.setUsersId(usersId);

        userMatchEntity.setMatches(matches);

        userMatchEntity = userMatchRepository.insert(userMatchEntity);
        return userMatchEntity;
    }

    private List<MatchEntity> matchMap(List<Choice> choices1, List<Choice> choices2, MatchMethod matchMethod) {
        List<MatchEntity> matches = new ArrayList<>();
        for (Area area : Area.values()) {
            matches.add(getMatchForMatchMethod(choices1, choices2, matchMethod, area));
        }
        return matches;
    }

    private MatchEntity getMatchForMatchMethod(List<Choice> choices1, List<Choice> choices2, MatchMethod matchMethod, Area area) {
        MatchEntity matchEntity = new MatchEntity();
        matchEntity.setArea(area);

        Result result = new Result();
        if(matchMethod == MatchMethod.PERCENT){
            result.setNumber(areaMatchPercent(choices1, choices2, area));}

        else if(matchMethod == MatchMethod.PEARSONCORRELATION){
            result.setNumber(areaMatchPearson(choices1, choices2, area));
        } else throw new BadRequestException(env.getProperty("error.ThereIsn'tThatMatchMethod"));

        matchEntity.setResult(result);
        matchEntity.setMatchMethod(matchMethod);
        return matchEntity;
    }

    private Double areaMatchPearson(List<Choice> choices1, List<Choice> choices2, Area area){

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
    private int numbOfSecondScaleMatchesInTwoSets(List<Choice> choices1, List<Choice> choices2, Area area) {

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
    private int numOfSecondScaleMatches(List<Choice> choices, Area area) {

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
    private Double areaMatchPercent(List<Choice> choices1, List<Choice> choices2, Area area){

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
    private int getTotalSize(Area area, List<Choice> choices1, List<Choice> choices2) {
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
    private int numbOfMatchesInTwoSets(List<Choice> choices1, List<Choice> choices2, Area area) {

        if(area == null || area == Area.TOTAL) {
           return (int) choices1.stream()
                    .filter(choice -> ifChoicesContainChoiceConsideringScaleShuffle(choices2, choice))
                    .count();
        }

       return (int) choices1.stream()
                .filter(choice -> (choice.getArea().equals(area)
                        && ifChoicesContainChoiceConsideringScaleShuffle(choices2, choice)))
                .count();
    }

    /**
     * If choices contain choice considering first and second scale shuffle.
     * @param choices list of Choices, in which we are looking for a choice
     * @param choice we are looking for
     * @return true if choices contain choice considering first and second scale shuffle, else return false
     */
    private Boolean ifChoicesContainChoiceConsideringScaleShuffle(List<Choice> choices, Choice choice) {
        return  choices.contains(choice) ||
                choices.contains(new Choice(choice.getArea(), choice.getSecondScale(), choice.getFirstScale(), choice.getChosenScale()));
    }

    private List<ValueCompatibilityAnswersEntity> getValueCompatibilityAnswers(User user){
//        Optional<List<ValueCompatibilityAnswersEntity>> valueCompatibilityAnswers = valueCompatibilityAnswersRepository
//                .findAllByUserIdOrderByIdDesc(user.getId());
//        if(!valueCompatibilityAnswers.isPresent()) {
//            throw new BadRequestException(env.getProperty("error.noTestWasPassed"));
//        } else return valueCompatibilityAnswers.get();
        return valueCompatibilityAnswersRepository.findAllByUserIdOrderByIdDesc(user.getId())
                .orElseThrow(() -> new BadRequestException(env.getProperty("error.noTestWasPassed")));
    }

}
