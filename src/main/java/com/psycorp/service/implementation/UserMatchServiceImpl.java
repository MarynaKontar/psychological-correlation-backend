package com.psycorp.service.implementation;

import com.psycorp.exception.BadRequestException;
import com.psycorp.model.entity.*;
import com.psycorp.model.enums.Area;
import com.psycorp.model.enums.MatchMethod;
import com.psycorp.model.objects.Matching;
import com.psycorp.model.objects.UserMatch;
import com.psycorp.repository.UserMatchRepository;
import com.psycorp.service.UserMatchService;
import com.psycorp.service.UserService;
import com.psycorp.service.ValueCompatibilityAnswersService;
import com.psycorp.util.UserMatchCommentUtil;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation for UserMatchService.
 * @author Maryna Kontar
 */
@Service
public class UserMatchServiceImpl implements UserMatchService {

    private final UserMatchRepository userMatchRepository;
    private final UserService userService;
    private final ValueCompatibilityAnswersService valueCompatibilityAnswersService;

    private final Environment env;

    @Autowired
    public UserMatchServiceImpl(UserMatchRepository userMatchRepository,
                                UserService userService,
                                ValueCompatibilityAnswersService valueCompatibilityAnswersService,
                                Environment env) {
        this.userMatchRepository = userMatchRepository;
        this.userService = userService;
        this.valueCompatibilityAnswersService = valueCompatibilityAnswersService;
        this.env = env;
    }

    /**
     * Matches results of value compatibility tests for user and principal user by matchMethod.
     * @param user must not be {@literal null}.
     * @param matchMethod must not be {@literal null}.
     * @return {@link UserMatch}.
     */
    @Override
    @Transactional
    public UserMatch match(User user, MatchMethod matchMethod){
        User principal = userService.getPrincipalUser();

        User user2;
        if (user != null) { // if get user from controller, than get it from db;
            if(user.getId() == null) { //if user from controller (from frontend) isn't has id
                user2 = userService.findUserByNameOrEmail(user.getName());
            } else {
                user2 = userService.find(user);
            }

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

        //last saved ValueCompatibilityAnswersEntity
        ValueCompatibilityAnswersEntity valueCompatibilityAnswersEntity1 = valueCompatibilityAnswersService.getLastPassedTest(principal);
        ValueCompatibilityAnswersEntity valueCompatibilityAnswersEntity2 = valueCompatibilityAnswersService.getLastPassedTest(user2);

        List<UserMatchEntity> userMatchEntitiesPrincipal = userMatchRepository.findByUsersIdContaining(principal.getId());
        List<UserMatchEntity> userMatchEntitiesUser2 = userMatchRepository.findByUsersIdContaining(user2.getId());

        // check if there is record in userMatchEntity collection that is after of both valueCompatibilityAnswers; if not - insert it
        User finalPrincipal = principal;
        User finalUser = user2;
//        LocalDateTime date1 = LocalDateTime.ofInstant(userMatchEntitiesPrincipal.get(0).getId().getDate().toInstant(), ZoneId.systemDefault());
//        && userMatchPrincipal.getId().getDate().after(Timestamp.valueOf(valueCompatibilityAnswersEntity1.getPassDate()))
//                && userMatchPrincipal.getId().getDate().after(Timestamp.valueOf(valueCompatibilityAnswersEntity2.getPassDate()))
        UserMatchEntity userMatchEntity
        = userMatchEntitiesPrincipal.stream()
                .filter(userMatchPrincipal ->
                    userMatchEntitiesUser2.contains(userMatchPrincipal)
                    && userMatchPrincipal.getId().compareTo(valueCompatibilityAnswersEntity1.getId()) >= 1
                    && userMatchPrincipal.getId().compareTo(valueCompatibilityAnswersEntity2.getId()) >= 1)
                .sorted(Comparator.comparing(UserMatchEntity::getId).reversed()).limit(1).findFirst()
                .orElseGet(() ->
                        insert(valueCompatibilityAnswersEntity1, valueCompatibilityAnswersEntity2, finalPrincipal, finalUser, matchMethod)
                );
        return getUserMatch(userMatchEntity);
    }

    /**
     * Validates whether user1 and user2 can be matched.
     * @param user1 must not be {@literal null}.
     * @param user2 must not be {@literal null}.
     * @throws BadRequestException if user1 or user2 is {@literal nul} or if they cann't be matched.
     */
    private void validIfUsersCanBeMatching(User user1, User user2) {
        if (user1.getUsersForMatchingId() == null || user2.getUsersForMatchingId() == null) {
            throw new BadRequestException(env.getProperty("error.UsersCan`tBeMatching"));
        }
        if (user1.getUsersForMatchingId() != null && user2.getUsersForMatchingId() != null) {
            List<ObjectId> ids1 = new ArrayList<>(user1.getUsersForMatchingId());
            List<ObjectId> ids2 = new ArrayList<>(user2.getUsersForMatchingId());

            //TODO пропустит только если у user1 в UsersForMatching есть user2 и наоборот. В дальнейшем добавить, чтобы пропускало, если профили OPEN
            if (!ids1.contains(user2.getId()) || !ids2.contains(user1.getId())) {
                throw new BadRequestException(env.getProperty("error.UsersCan`tBeMatching"));
            }
        }
    }

    /**
     * Insert new {@link UserMatchEntity} for user and principal user with related value compatibility test results.
     * @param valueCompatibilityAnswersEntity1 value compatibility test results for one of user or principal user,
     * must not be {@literal null}.
     * @param valueCompatibilityAnswersEntity2 value compatibility test results for one of user or principal user
     * (for not as in valueCompatibilityAnswersEntity1), must not be {@literal null}.
     * @param principal must not be {@literal null}.
     * @param user must not be {@literal null}.
     * @param matchMethod must not be {@literal null}.
     * @return saved {@link UserMatchEntity}.
     */
    private UserMatchEntity insert(ValueCompatibilityAnswersEntity valueCompatibilityAnswersEntity1,
                                   ValueCompatibilityAnswersEntity valueCompatibilityAnswersEntity2,
                                   User principal,
                                   User user,
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

    /**
     * Converts {@link UserMatchEntity} to {@link UserMatch}.
     * @param userMatchEntity must not be {@literal null}.
     * @return {@link UserMatch}.
     */
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

    /**
     * Retrieves users for all usersId.
     * @param usersId must not be {@literal null}.
     * @return set of {@link User}.
     */
    private Set<User> getUsers(Set<ObjectId> usersId) {
        return usersId
                .stream()
                .map(userService::findById)
                .collect(Collectors.toSet());
    }

    /**
     *
     * @param choices1 must not be {@literal null}.
     * @param choices2 must not be {@literal null}.
     * @param matchMethod must not be {@literal null}.
     * @return list of {@link MatchEntity}.
     */
    private List<MatchEntity> matchMap(List<Choice> choices1,
                                       List<Choice> choices2,
                                       MatchMethod matchMethod) {
        List<MatchEntity> matches = new ArrayList<>();
        for (Area area : Area.values()) {
            matches.add(getMatchForMatchMethod(choices1, choices2, matchMethod, area));
        }
        return matches;
    }

    /**
     * Creates {@link MatchEntity} for given area, matchMethod and test results (choice1 and choice2).
     * @param choices1 must not be {@literal null}.
     * @param choices2 must not be {@literal null}.
     * @param matchMethod must not be {@literal null}.
     * @param area must not be {@literal null}.
     * @return {@link MatchEntity}.
     */
    private MatchEntity getMatchForMatchMethod(List<Choice> choices1,
                                               List<Choice> choices2,
                                               MatchMethod matchMethod,
                                               Area area) {
        MatchEntity matchEntity = new MatchEntity();
        matchEntity.setArea(area);

        Result result = new Result();
        switch (matchMethod) {
            case PERCENT:
                result.setNumber(areaMatchPercent(choices1, choices2, area));
                break;
            case PEARSONCORRELATION:
                result.setNumber(areaMatchPearson(choices1, choices2, area));
                break;
            default:
                throw new BadRequestException(env.getProperty("error.ThereIsn'tThatMatchMethod"));
        }

        matchEntity.setResult(result);
        matchEntity.setMatchMethod(matchMethod);
        return matchEntity;
    }

    /**
     * Counts test result for PEARSON {@link MatchMethod} for this area.
     * @param choices1 must not be {@literal null}.
     * @param choices2 must not be {@literal null}.
     * @param area must not be {@literal null}.
     * @return test result for PEARSON {@link MatchMethod} for this area.
     */
    private Double areaMatchPearson(List<Choice> choices1,
                                    List<Choice> choices2,
                                    Area area){
        int totalSize = getTotalSize(area, choices1, choices2);

        double numOfMatches = (double) numbOfSecondScaleMatchesInTwoSets(choices1, choices2, area) / totalSize;
        double numOfSecondScaleMatchesForUser1 = (double) numOfSecondScaleMatches(choices1, area) / totalSize;
        double numOfSecondScaleMatchesForUser2 = (double) numOfSecondScaleMatches(choices2, area) / totalSize;

        Double pearsonCorrelationCoefficient = getPearsonCorrelationCoefficient(numOfMatches,
                numOfSecondScaleMatchesForUser1, numOfSecondScaleMatchesForUser2);

        return pearsonCorrelationCoefficient;
    }

    /**
     *
     * @param numberOfMatches must not be {@literal null}.
     * @param numOfSecondScaleMatchesForUser1 must not be {@literal null}.
     * @param numOfSecondScaleMatchesForUser2 must not be {@literal null}.
     * @return
     */
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
     * Count the number of choices for area, where chosenScale is secondScale for both choices1 and choices2.
     * @param choices1 must not be {@literal null}.
     * @param choices2 must not be {@literal null}.
     * @param area must not be {@literal null}.
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
     * @param choices must not be {@literal null}.
     * @param area must not be {@literal null}.
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
     * @param choices1 must not be {@literal null}.
     * @param choices2 must not be {@literal null}.
     * @param area must not be {@literal null}.
     * @return
     */
    private Double areaMatchPercent(List<Choice> choices1, List<Choice> choices2, Area area){

        int totalSize = getTotalSize(area, choices1, choices2);

        int numbOfMatches = numbOfMatchesInTwoSets(choices1, choices2, area);
        Double MatchPercent = (double) 100 * numbOfMatches / totalSize;
        return MatchPercent;
    }

    /**
     * Gets amount of choices in choices1 and choices2 that has area
     * @param area must not be {@literal null}.
     * @param choices1 must not be {@literal null}.
     * @param choices2 must not be {@literal null}.
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
     * @param choices1 must not be {@literal null}.
     * @param choices2 must not be {@literal null}.
     * @param area must not be {@literal null}.
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
     * Returns whether choices contain choice considering first and second scale shuffle.
     * @param choices list of {@link Choice}, in which we are looking for a choice, must not be {@literal null}.
     * @param choice  the choice we are looking for, must not be {@literal null}.
     * @return {@literal true} if choices contain choice considering first and second scale shuffle,
     * {@literal false} otherwise.
     */
    private Boolean ifChoicesContainChoiceConsideringScaleShuffle(List<Choice> choices, Choice choice) {
        return  choices.contains(choice) ||
                choices.contains(new Choice(choice.getArea(), choice.getSecondScale(), choice.getFirstScale(), choice.getChosenScale()));
    }
}
