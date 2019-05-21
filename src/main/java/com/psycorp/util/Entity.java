package com.psycorp.util;

import com.psycorp.model.entity.Choice;
import com.psycorp.model.entity.User;
import com.psycorp.model.entity.ValueCompatibilityAnswersEntity;
import com.psycorp.model.enums.Area;
import com.psycorp.model.enums.Scale;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Class for entities creation when test application
 */


public class Entity {

    /**
     *
     * @param user
     * @return ValueCompatibilityAnswersEntity for user
     */
    public static ValueCompatibilityAnswersEntity createRandomUserAnswers(User user){
        ValueCompatibilityAnswersEntity valueCompatibilityAnswersEntity = new ValueCompatibilityAnswersEntity();
        valueCompatibilityAnswersEntity.setUserId(user.getId());

        List<Choice> choices = new ArrayList<>();
        choices.addAll(getChoices(Area.GOAL));
        choices.addAll(getChoices(Area.QUALITY));
        choices.addAll(getChoices(Area.STATE));

        valueCompatibilityAnswersEntity.setUserAnswers(choices);
        valueCompatibilityAnswersEntity.setCreationDate(LocalDateTime.now());
        valueCompatibilityAnswersEntity.setPassDate(LocalDateTime.now());

        return valueCompatibilityAnswersEntity;
    }

    private static Set<Choice> getChoices(Area area) {

        Choice choice1 = getChoice(area, Scale.ONE, Scale.TWO);
        Choice choice2 = getChoice(area, Scale.ONE, Scale.THREE);
        Choice choice3 = getChoice(area, Scale.ONE, Scale.FOUR);
        Choice choice4 = getChoice(area, Scale.ONE, Scale.FIVE);
        Choice choice5 = getChoice(area, Scale.ONE, Scale.SIX);

        Choice choice6 = getChoice(area, Scale.TWO, Scale.THREE);
        Choice choice7 = getChoice(area, Scale.TWO, Scale.FOUR);
        Choice choice8 = getChoice(area, Scale.TWO, Scale.FIVE);
        Choice choice9 = getChoice(area, Scale.TWO, Scale.SIX);

        Choice choice10 = getChoice(area, Scale.THREE, Scale.FOUR);
        Choice choice11 = getChoice(area, Scale.THREE, Scale.FIVE);
        Choice choice12 = getChoice(area, Scale.THREE, Scale.SIX);

        Choice choice13 = getChoice(area, Scale.FOUR, Scale.FIVE);
        Choice choice14 = getChoice(area, Scale.FOUR, Scale.SIX);

        Choice choice15 = getChoice(area, Scale.FIVE, Scale.SIX);

//        choice1.setChosenScale(Scale.TWO);
//        choice2.setChosenScale(Scale.THREE);
//        choice3.setChosenScale(Scale.FOUR);
//        choice4.setChosenScale(Scale.FIVE);
//        choice5.setChosenScale(Scale.SIX);
//        choice6.setChosenScale(Scale.THREE);
//        choice7.setChosenScale(Scale.FOUR);
//        choice8.setChosenScale(Scale.FIVE);
//        choice9.setChosenScale(Scale.SIX);
//        choice10.setChosenScale(Scale.FOUR);
//        choice11.setChosenScale(Scale.FIVE);
//        choice12.setChosenScale(Scale.SIX);
//        choice13.setChosenScale(Scale.FIVE);
//        choice14.setChosenScale(Scale.SIX);
//        choice15.setChosenScale(Scale.FIVE);

        return new HashSet<>(Arrays.asList(choice1, choice2, choice3, choice4, choice5,
                choice6, choice7, choice8, choice9, choice10, choice11, choice12, choice13, choice14, choice15));
    }

    private static Choice getChoice(Area area, Scale scaleOne, Scale scaleTwo) {
        Choice choice = new Choice();
        choice.setArea(area);
        choice.setFirstScale(scaleOne);
        choice.setSecondScale(scaleTwo);
        choice.setChosenScale(chooseRandomScale(choice));
        return choice;
    }

    private static Scale chooseRandomScale(Choice choice){
        Boolean random = new Random().nextBoolean();

        return random ? choice.getFirstScale() : choice.getSecondScale();
//        return choice.getFirstScale();
    }
}
