package com.psycorp.util;

import com.psycorp.exception.BadRequestException;
import com.psycorp.model.entity.MatchEntity;
import com.psycorp.model.entity.Result;
import com.psycorp.model.enums.AspectLevel;
import com.psycorp.model.enums.Scale;
import com.psycorp.model.enums.ScaleLevel;
import com.psycorp.model.objects.UserMatchComment;
import com.psycorp.model.objects.ValuesDifferencesComment;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

import java.util.Arrays;

/**
 * Util class for getting comment on user match for some Area or Scale
 */
public class UserMatchCommentUtil {

    public static UserMatchComment getAspectComment(MatchEntity matchEntity, Environment env){
        Assert.isTrue(matchEntity.getResult().getNumber() <= 100, "Value is a percent of chosen scale and can't be > 100.");
        String area = matchEntity.getArea().toString().toLowerCase();
        String levelPrefix = getAspectLevelPrefix(matchEntity.getResult(), env);

        UserMatchComment userMatchComment = new UserMatchComment();
        userMatchComment.setAspect(env.getProperty(area));
//        userMatchComment.setResult(matchEntity.getResult().getNumber());
        userMatchComment.setLevel(AspectLevel.valueOf(levelPrefix.toUpperCase()));
        userMatchComment.setLevelName(env.getProperty(levelPrefix));
        userMatchComment.setAspectDescription(env.getProperty(area + ".aspectDescription"));
        userMatchComment.setHeader(env.getProperty(area + "." + levelPrefix + ".header"));
        userMatchComment.setForeword(env.getProperty(area + "." + levelPrefix + ".foreword"));
        userMatchComment.setAccent(env.getProperty(area + "." + levelPrefix + ".accent"));
        userMatchComment.setMainText(env.getProperty(area + "." + levelPrefix + ".mainText"));
        return userMatchComment;
    }

    public static ValuesDifferencesComment getScaleComment(Scale scale, Integer value1, Integer value2, Environment env){

        Integer result = Math.abs(value1 - value2);
        String levelPrefix = getScaleLevelPrefix(result, env);

        ValuesDifferencesComment valuesDifferencesComment = new ValuesDifferencesComment();
        valuesDifferencesComment.setScale(env.getProperty(scale.name().toLowerCase())); //one - Безопасности
        valuesDifferencesComment.setResult(result);
        valuesDifferencesComment.setLevel(ScaleLevel.valueOf(levelPrefix.toUpperCase()));
        valuesDifferencesComment.setLevelName(env.getProperty(levelPrefix)); //full_match = Полное совпадение
        valuesDifferencesComment.setText(Arrays.asList(
                env.getProperty(levelPrefix + ".text.1"),
                env.getProperty(levelPrefix + ".text.2"),
                env.getProperty(levelPrefix + ".text.3"),
                env.getProperty(levelPrefix + ".text.4"))
        );

        return valuesDifferencesComment;
    }

    private static String getAspectLevelPrefix(Result result, Environment env) {
        // если % (resultNumber) от 0 до low.value, то уровень (level) = low,
        // если от low.value до sufficient.value - то level = sufficient,
        // если от sufficient.value до good.value - то level = good,
        // если больше good.value - то level = excellent
        Double resultNumber = result.getNumber();
        String levelPrefix;

        if (resultNumber < Integer.valueOf(env.getProperty("low.value"))) { // <40
            levelPrefix = env.getProperty("low.level");
        } else if(resultNumber < Integer.valueOf(env.getProperty("sufficient.value"))) { // <60
            levelPrefix = env.getProperty("sufficient.level");
        } else if(resultNumber < Integer.valueOf(env.getProperty("good.value"))) { // <80
            levelPrefix = env.getProperty("good.level");
        } else levelPrefix = env.getProperty("excellent.level");

        return levelPrefix;
    }

    private static String getScaleLevelPrefix(Integer result, Environment env) {
        // если result=[0, minor_differences.value), то уровень (level) = full_match,
        // если result=[minor_differences.value, moderate_differences.value) - то level = minor_differences,
        // если result=[moderate_differences.value, strong_differences)  - то level = moderate_differences,
        // если result=[strong_differences, total.number.of.questions) - то level = strong_differences

        String levelPrefix;

        if (result < Integer.valueOf(env.getProperty("minor_differences.value")) && result >= 0) { // <1 (=0)
            levelPrefix = env.getProperty("full_match.level");
        } else if(result < Integer.valueOf(env.getProperty("moderate_differences.value"))) { // <4 (1-3)
            levelPrefix = env.getProperty("minor_differences.level");
        } else if(result < Integer.valueOf(env.getProperty("strong_differences.value"))) { // <8 (4-7)
            levelPrefix = env.getProperty("moderate_differences.level");
        } else if(result <= Integer.valueOf(env.getProperty("total.number.of.questions"))) {
            levelPrefix = env.getProperty("strong_differences.level");
        } else throw new IllegalArgumentException(env.getProperty("error.IllegalRangeDifferencesForScale") + ": " + result);

        return levelPrefix;
    }
}
