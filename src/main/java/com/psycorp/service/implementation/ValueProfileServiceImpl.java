package com.psycorp.service.implementation;

import com.psycorp.model.entity.*;
import com.psycorp.model.enums.Scale;
import com.psycorp.model.objects.*;
import com.psycorp.service.UserAnswersService;
import com.psycorp.service.UserService;
import com.psycorp.service.ValueProfileService;
import com.psycorp.util.UserMatchCommentUtil;
import com.psycorp.util.ValueProfileCommentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Map.Entry.comparingByKey;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

@Service
@PropertySource(value = {"classpath:testing/scalesquestionsukrainian.properties"}, encoding = "utf-8", ignoreResourceNotFound = true)
@PropertySource(value = {"classpath:testing/scalesquestionsrussian.properties"}, encoding = "utf-8")
@PropertySource(value = {"classpath:testing/scalesenglish.properties"}, encoding = "utf-8", ignoreResourceNotFound = true)
@PropertySource(value = {"classpath:valueprofile/scalescommentrussian.properties"}, encoding = "utf-8", ignoreResourceNotFound = true)
@PropertySource(value = {"classpath:match/areacommentrussian.properties"}, encoding = "utf-8")
@PropertySource(value = {"classpath:match/scalescommentrussian.properties"}, encoding = "utf-8")
public class ValueProfileServiceImpl implements ValueProfileService {

    private final UserService userService;
    private final UserAnswersService userAnswersService;
    private final Environment env;

    @Autowired
    public ValueProfileServiceImpl(UserService userService, UserAnswersService userAnswersService, Environment env) {
        this.userService = userService;
        this.userAnswersService = userAnswersService;
        this.env = env;
    }

    /**
     * Return ValueProfile with comments for individual value profile
     * @param noPrincipalUser
     * @return
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

    @Override
    public ValueProfileMatching getValueProfileForMatching(User noPrincipalUser) {

        noPrincipalUser = userService.find(noPrincipalUser);
        User principalUser = userService.getPrincipalUser();

        ValueProfile valueProfileForNoPrincipalUser = getValueProfile(noPrincipalUser, false);
        ValueProfile valueProfileForPrincipalUser = getValueProfile(principalUser, true);

        return convertToValueProfileMatching(noPrincipalUser, principalUser,
                valueProfileForNoPrincipalUser, valueProfileForPrincipalUser);
    }

    private ValueProfile getValueProfile(User user, Boolean isPrincipalUser){
        UserAnswersEntity userAnswer = userAnswersService.getLastPassedTest(user);

        Map<Scale, Result> valueProfile = new HashMap<>();
        final Integer totalNumberOfQuestions = Integer.valueOf(env.getProperty("total.number.of.questions"));

        userAnswer.getUserAnswers()
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

    private  Map<Scale, Integer> getScaleValues(User user){

        UserAnswersEntity userAnswerPrincipal = userAnswersService.getLastPassedTest(user);
        Map<Scale, Integer> scaleValues = new HashMap<>();

        userAnswerPrincipal.getUserAnswers()
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






//    @Override
//    public ValueProfile getValueProfileIndividual(User noPrincipalUser) {
//
//        // if there is noPrincipalUser - get it; if not - get principal
//        User user;
//        Boolean isPrincipalUser;
//        if (noPrincipalUser != null) {
//            user = userService.find(noPrincipalUser);
//            isPrincipalUser = false;
//        } else {
//            user = userService.getPrincipalUser();
//            isPrincipalUser = true;
//        }
//
//        return getValueProfileIndividual(user, isPrincipalUser);
//    }
//
//    @Override
//    public ValueProfileMatching getValueProfileForMatching(User noPrincipalUser) {
//
//        noPrincipalUser = userService.find(noPrincipalUser);
//        User principalUser = userService.getPrincipalUser();
//
//        ValueProfile valueProfileForNoPrincipalUser = getValueProfileIndividual(noPrincipalUser, false);
//        ValueProfile valueProfileForPrincipalUser = getValueProfileIndividual(principalUser, true);
//        ValueProfileMatching valueProfileMatching = new ValueProfileMatching();
//        valueProfileMatching.setValueProfileList(Arrays.asList(valueProfileForPrincipalUser,valueProfileForNoPrincipalUser));
//        return new ValueProfileMatching();
//    }
//
//
//    private ValueProfile getValueProfileIndividual(User user, Boolean isPrincipalUser){
//        UserAnswersEntity userAnswer = userAnswersService.getLastPassedTest(user);
//
//        Map<Scale, Result> valueProfile = new HashMap<>();
//        final Integer totalNumberOfQuestions = Integer.valueOf(env.getProperty("total.number.of.questions"));
//
//        userAnswer.getUserAnswers()
//                .stream()
//                .collect(groupingBy(choice -> choice.getChosenScale(), counting()))
//                .forEach((scale, value) ->
//                        valueProfile.put(scale, new Result(value.doubleValue()/totalNumberOfQuestions * 100)));
//
//        // if user didn't choose some scale at all, than we have to put this scale (testing) to answer with 0 value
//        List<Scale> scales = getScales();
//        scales.forEach(scale -> valueProfile.putIfAbsent(scale, new Result(0d)));
//
//
//        //sort by scale in descending order
//        Map<Scale, Result> sortedValueProfile = valueProfile.entrySet().stream()
//                .sorted(comparingByKey(Comparator.reverseOrder()))
//                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
//                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
//
//        //        Map<Scale, ValueProfileComment> valueProfile = new HashMap<>();
////        final Integer totalNumberOfQuestions = Integer.valueOf(env.getProperty("total.number.of.questions"));
////
////        userAnswer.getUserAnswers()
////                .stream()
////                .collect(groupingBy(choice -> choice.getChosenScale(), counting()))
////                .forEach((scale, value) ->
////                        valueProfile.put(scale,
////                                ValueProfileCommentUtil.getComment(env, scale, value.doubleValue()/totalNumberOfQuestions * 100)));
////
////        // if user didn't choose some scale at all, than we have to put this scale (testing) to answer with 0 value
////        List<Scale> scales = getScales();
////        scales.forEach(scale -> valueProfile.putIfAbsent(scale, ValueProfileCommentUtil.getComment(env, scale, 0d)));
//
//        //sort by scale in descending order
////        Map<Scale, ValueProfileComment> sortedValueProfile = valueProfile.entrySet().stream()
////                .sorted(comparingByKey(Comparator.reverseOrder()))
////                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
////                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
//
//        return new ValueProfile(sortedValueProfile, isPrincipalUser);
//    }