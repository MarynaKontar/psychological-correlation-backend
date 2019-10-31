package com.psycorp.service.implementation;

import com.psycorp.model.entity.*;
import com.psycorp.model.enums.Scale;
import com.psycorp.model.objects.*;
import com.psycorp.service.ValueCompatibilityAnswersService;
import com.psycorp.service.UserService;
import com.psycorp.service.ValueProfileService;
import com.psycorp.util.UserMatchCommentUtil;
import com.psycorp.util.ValueProfileCommentUtil;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Map.Entry.comparingByKey;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

/**
 * Service implementation for ValueProfileService.
 * @author Maryna Kontar
 */
@Service
@PropertySource(value = {"classpath:testing/scalesquestionsukrainian.properties"}, encoding = "utf-8", ignoreResourceNotFound = true)
@PropertySource(value = {"classpath:testing/scalesquestionsrussian.properties"}, encoding = "utf-8")
@PropertySource(value = {"classpath:testing/scalesenglish.properties"}, encoding = "utf-8", ignoreResourceNotFound = true)
@PropertySource(value = {"classpath:valueprofile/scalescommentrussian.properties"}, encoding = "utf-8", ignoreResourceNotFound = true)
@PropertySource(value = {"classpath:match/areacommentrussian.properties"}, encoding = "utf-8")
@PropertySource(value = {"classpath:match/scalescommentrussian.properties"}, encoding = "utf-8")
public class ValueProfileServiceImpl implements ValueProfileService {

    private final UserService userService;
    private final ValueCompatibilityAnswersService valueCompatibilityAnswersService;
    private final Environment env;

    @Autowired
    public ValueProfileServiceImpl(UserService userService,
                                   ValueCompatibilityAnswersService valueCompatibilityAnswersService,
                                   Environment env) {
        this.userService = userService;
        this.valueCompatibilityAnswersService = valueCompatibilityAnswersService;
        this.env = env;
    }

    /**
     * Returns {@link ValueProfileIndividual} with comments for noPrincipalUser
     * if it isn't {@literal null} or for principal user if it is.
     * @param noPrincipalUser equals {@literal null} for principal user.
     * @return {@link ValueProfileIndividual}.
     */
    @Override
    public ValueProfileIndividual getValueProfileIndividual(User noPrincipalUser) {

        // if there is noPrincipalUser - get it; if not - get principal
        User user;
        Boolean isPrincipalUser;
        if (noPrincipalUser != null) {
            user = userService.find(noPrincipalUser);
            isPrincipalUser = false;
        } else {
            user = userService.getPrincipalUser();
            isPrincipalUser = true;
        }
        ValueProfile valueProfile = getValueProfile(user, isPrincipalUser);

        return convertToValueProfileIndividual(valueProfile);
    }

    /**
     * Returns {@link ValueProfileMatching} with comments for user with noPrincipalUserId and principal user.
     * @param noPrincipalUserId must not be {@literal null}.
     * @return {@link ValueProfileMatching}.
     */
    @Override
    public ValueProfileMatching getValueProfileForMatching(ObjectId noPrincipalUserId) {
        User noPrincipalUser = userService.findById(noPrincipalUserId);
        User principalUser = userService.getPrincipalUser();

        ValueProfile valueProfileForNoPrincipalUser = getValueProfile(noPrincipalUser, false);
        ValueProfile valueProfileForPrincipalUser = getValueProfile(principalUser, true);

        return convertToValueProfileMatching(noPrincipalUser, principalUser,
                valueProfileForNoPrincipalUser, valueProfileForPrincipalUser);
    }


    /**
     * Gets value profile for user.
     * @param user must not be {@literal null}.
     * @param isPrincipalUser is {@literal true} for principal user and {@literal false} for no principal user.
     * @return {@link ValueProfile}.
     */
    private ValueProfile getValueProfile(User user, Boolean isPrincipalUser){
        ValueCompatibilityAnswersEntity answersEntity = valueCompatibilityAnswersService.getLastPassedTest(user);

        Map<Scale, Result> valueProfile = new HashMap<>();
        final Integer totalNumberOfQuestions = Integer.valueOf(env.getProperty("total.number.of.questions"));

        answersEntity.getUserAnswers()
                .stream()
                .collect(groupingBy(choice -> choice.getChosenScale(), counting()))
                .forEach((scale, value) ->
                        valueProfile.put(scale, new Result(value.doubleValue()/totalNumberOfQuestions * 100)));

        // if user didn't choose some scale at all, than we have to put this scale (testing) to answer with 0 value
        List<Scale> scales = getScales();
        scales.forEach(scale -> valueProfile.putIfAbsent(scale, new Result(0d)));

        //sort by scale in descending order
        Map<Scale, Result> sortedValueProfile = valueProfile.entrySet().stream()
                .sorted(comparingByKey(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        return new ValueProfile(user, sortedValueProfile, isPrincipalUser);
    }

    /**
     * Creates list of {@link Scale} with all scale values.
     * @return list of {@link Scale}.
     */
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

    /**
     * Converts {@link ValueProfile} to {@link ValueProfileIndividual}.
     * @param valueProfile must not be {@literal null}.
     * @return {@link ValueProfileIndividual}.
     */
    private ValueProfileIndividual convertToValueProfileIndividual(ValueProfile valueProfile) {
        ValueProfileIndividual valueProfileIndividual = new ValueProfileIndividual();
        valueProfileIndividual.setValueProfile(valueProfile);

        List<ValueProfileComment> valueProfileCommentList = new ArrayList<>();
        valueProfile.getScaleResult()
                .forEach((key, value) -> valueProfileCommentList
                        .add(ValueProfileCommentUtil.getComment(env, key, value.getNumber())));

        valueProfileIndividual.setValueProfileCommentList(valueProfileCommentList);
        return valueProfileIndividual;
    }

    /**
     * Converts {@link ValueProfile} for principal and not principal user to {@link ValueProfileMatching}.
     * @param noPrincipalUser must not be {@literal null}.
     * @param principalUser must not be {@literal null}.
     * @param valueProfileForNoPrincipalUser must not be {@literal null}.
     * @param valueProfileForPrincipalUser must not be {@literal null}.
     * @return {@link ValueProfileMatching}.
     */
    private ValueProfileMatching convertToValueProfileMatching(User noPrincipalUser, User principalUser,
                                                               ValueProfile valueProfileForNoPrincipalUser,
                                                               ValueProfile valueProfileForPrincipalUser) {
        ValueProfileMatching valueProfileMatching = new ValueProfileMatching();
        valueProfileMatching.setValueProfileList(Arrays.asList(valueProfileForPrincipalUser,valueProfileForNoPrincipalUser));

        List<ValuesDifferencesComment> valuesDifferencesCommentList = new ArrayList<>();
        Map<Scale, Integer> scaleValuesForNoPrincipalUser = getScaleValues(noPrincipalUser);
        Map<Scale, Integer> scaleValuesForPrincipalUser = getScaleValues(principalUser);

        scaleValuesForPrincipalUser
                .forEach((scale, value) -> valuesDifferencesCommentList
                        .add(UserMatchCommentUtil.getScaleComment(scale, value, scaleValuesForNoPrincipalUser.get(scale), env)));

        valueProfileMatching.setValuesDifferencesCommentList(valuesDifferencesCommentList);
        return valueProfileMatching;
    }

    /**
     * Counts how many times there are values for each {@link Scale}
     * in the last passed value compatibility test for given user.
     * And put these {@link Scale} - value pairs to {@link Map}.
     * @param user must not be {@literal null}.
     * @return {@link Map<Scale, Integer>}.
     */
    private  Map<Scale, Integer> getScaleValues(User user){

        ValueCompatibilityAnswersEntity answersEntityPrincipal = valueCompatibilityAnswersService.getLastPassedTest(user);
        Map<Scale, Integer> scaleValues = new HashMap<>();

        answersEntityPrincipal.getUserAnswers()
                .stream()
                .collect(groupingBy(choice -> choice.getChosenScale(), counting()))
                .forEach((scale, value) ->
                        scaleValues.put(scale, value.intValue()));

        // if user didn't choose some scale at all, than we have to put this scale (testing) to answer with 0 value
        List<Scale> scales = getScales();
        scales.forEach(scale -> scaleValues.putIfAbsent(scale, 0));


        //sort by scale in descending order
        Map<Scale, Integer> sortedScaleValues = scaleValues.entrySet().stream()
                .sorted(comparingByKey(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        return sortedScaleValues;
    }
}
