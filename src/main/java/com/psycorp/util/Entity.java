package com.psycorp.util;

import com.psycorp.model.entity.Choice;
import com.psycorp.model.entity.User;
import com.psycorp.model.entity.UserAnswers;
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
     * @return UserAnswers for user
     */
    public static UserAnswers createUserAnswers(User user){
        UserAnswers userAnswers = new UserAnswers();
        userAnswers.setUser(user);

        Set<Choice> choices = new HashSet<>();
        choices.addAll(getChoices(Area.GOAL));
        choices.addAll(getChoices(Area.QUALITY));
        choices.addAll(getChoices(Area.STATE));

        userAnswers.setUserAnswers(choices);
        userAnswers.setCreationDate(LocalDateTime.now());
        userAnswers.setPassDate(LocalDateTime.now());

        return userAnswers;
    }

    private static Set<Choice> getChoices(Area area) {

        Choice choice1 = new Choice();
        choice1.setArea(area);
        choice1.setFirstScale(Scale.ONE);
        choice1.setSecondScale(Scale.TWO);
        choice1.setChosenScale(chooseRandomScale(choice1));

        Choice choice2 = new Choice();
        choice2.setArea(area);
        choice2.setFirstScale(Scale.ONE);
        choice2.setSecondScale(Scale.THREE);
        choice2.setChosenScale(chooseRandomScale(choice2));

        Choice choice3 = new Choice();
        choice3.setArea(area);
        choice3.setFirstScale(Scale.ONE);
        choice3.setSecondScale(Scale.FOUR);
        choice3.setChosenScale(chooseRandomScale(choice3));

        Choice choice4 = new Choice();
        choice4.setArea(area);
        choice4.setFirstScale(Scale.ONE);
        choice4.setSecondScale(Scale.FIVE);
        choice4.setChosenScale(chooseRandomScale(choice4));

        Choice choice5 = new Choice();
        choice5.setArea(area);
        choice5.setFirstScale(Scale.ONE);
        choice5.setSecondScale(Scale.SIX);
        choice5.setChosenScale(chooseRandomScale(choice5));

        Choice choice6 = new Choice();
        choice6.setArea(area);
        choice6.setFirstScale(Scale.TWO);
        choice6.setSecondScale(Scale.THREE);
        choice6.setChosenScale(chooseRandomScale(choice6));

        Choice choice7 = new Choice();
        choice7.setArea(area);
        choice7.setFirstScale(Scale.TWO);
        choice7.setSecondScale(Scale.FOUR);
        choice7.setChosenScale(chooseRandomScale(choice7));

        Choice choice8 = new Choice();
        choice8.setArea(area);
        choice8.setFirstScale(Scale.TWO);
        choice8.setSecondScale(Scale.FIVE);
        choice8.setChosenScale(chooseRandomScale(choice8));

        Choice choice9 = new Choice();
        choice9.setArea(area);
        choice9.setFirstScale(Scale.TWO);
        choice9.setSecondScale(Scale.SIX);
        choice9.setChosenScale(chooseRandomScale(choice9));

        Choice choice10 = new Choice();
        choice10.setArea(area);
        choice10.setFirstScale(Scale.THREE);
        choice10.setSecondScale(Scale.FOUR);
        choice10.setChosenScale(chooseRandomScale(choice10));

        Choice choice11 = new Choice();
        choice11.setArea(area);
        choice11.setFirstScale(Scale.THREE);
        choice11.setSecondScale(Scale.FIVE);
        choice11.setChosenScale(chooseRandomScale(choice11));

        Choice choice12 = new Choice();
        choice12.setArea(area);
        choice12.setFirstScale(Scale.THREE);
        choice12.setSecondScale(Scale.SIX);
        choice12.setChosenScale(chooseRandomScale(choice12));

        Choice choice13 = new Choice();
        choice13.setArea(area);
        choice13.setFirstScale(Scale.FOUR);
        choice13.setSecondScale(Scale.FIVE);
        choice13.setChosenScale(chooseRandomScale(choice13));

        Choice choice14 = new Choice();
        choice14.setArea(area);
        choice14.setFirstScale(Scale.FOUR);
        choice14.setSecondScale(Scale.SIX);
        choice14.setChosenScale(chooseRandomScale(choice14));

        Choice choice15 = new Choice();
        choice15.setArea(area);
        choice15.setFirstScale(Scale.FIVE);
        choice15.setSecondScale(Scale.SIX);
        choice15.setChosenScale(chooseRandomScale(choice15));

        return new HashSet<>(Arrays.asList(choice1, choice2, choice3, choice4, choice5,
                choice6, choice7, choice8, choice9, choice10, choice11, choice12, choice13, choice14, choice15));
    }


    private static Scale chooseRandomScale(Choice choice){
        Boolean random = new Random().nextBoolean();

        return random ? choice.getFirstScale() : choice.getSecondScale();
    }
}
