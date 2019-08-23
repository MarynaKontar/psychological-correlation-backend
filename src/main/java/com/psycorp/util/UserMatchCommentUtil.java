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
 * Util class for getting comment on users matching for some Area or Scale.
 * @author Maryna Kontar
 */
public class UserMatchCommentUtil {

    /**
     * Get aspect {@link com.psycorp.model.enums.Area} comment depending on result value of matchEntity.
     * @param matchEntity must not be {@literal null}.
     * @param env must not be {@literal null}.
     * @return user match comment.
     * @throws BadRequestException if scale or matchEntity are {@literal null}.
     * @throws NullPointerException if env is {@literal null}.
     * @throws IllegalArgumentException if matchEntity.getResult().getNumber() > 100.
     */
    public static UserMatchComment getAspectComment(MatchEntity matchEntity, Environment env){
        if (matchEntity == null) {throw new BadRequestException("MatchEntity can't be null.");}
        Assert.isTrue(matchEntity.getResult().getNumber() <= 100, "Value is a percent of chosen scale and can't be > 100.");
        String area = matchEntity.getArea().toString().toLowerCase();
        String levelPrefix = getAspectLevelPrefix(matchEntity.getResult(), env);

        UserMatchComment userMatchComment = new UserMatchComment();
        userMatchComment.setAspect(env.getProperty(area));
        userMatchComment.setLevel(AspectLevel.valueOf(levelPrefix.toUpperCase()));
        userMatchComment.setLevelName(env.getProperty(levelPrefix));
        userMatchComment.setAspectDescription(env.getProperty(area + ".aspectDescription"));
        userMatchComment.setHeader(env.getProperty(area + "." + levelPrefix + ".header"));
        userMatchComment.setForeword(env.getProperty(area + "." + levelPrefix + ".foreword"));
        userMatchComment.setAccent(env.getProperty(area + "." + levelPrefix + ".accent"));
        userMatchComment.setMainText(env.getProperty(area + "." + levelPrefix + ".mainText"));
        return userMatchComment;
    }

    /**
     * Get {@link ValuesDifferencesComment} for scale depending on the difference of value1 and value2.
     * @param scale must not be {@literal null}.
     * @param value1 must not be {@literal null}.
     * @param value2 must not be {@literal null}.
     * @param env must not be {@literal null}.
     * @return value differences comment.
     * @throws BadRequestException if scale or values are {@literal null}.
     * @throws NullPointerException if env is {@literal null}.
     */
    public static ValuesDifferencesComment getScaleComment(Scale scale, Integer value1, Integer value2, Environment env){
        if (value1 == null || value2 == null || scale == null) {throw new BadRequestException("Scale and values can't be null.");}
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

    /**
     * Get aspect (Area) level prefix for result.
     * if resultNumber from 0 to low.value, then level = low,
     * from low.value to sufficient.value - then level = sufficient,
     * from sufficient.value to good.value - then level = good,
     * more then good.value - then level = excellent
     * @param result must not be {@literal null}.
     * @param env must not be {@literal null}.
     * @return aspect level prefix value.
     * @throws BadRequestException if result is {@literal null}.
     * @throws NullPointerException if env is {@literal null}.
     */
    private static String getAspectLevelPrefix(Result result, Environment env) {
        if (result == null) {throw new BadRequestException("Result can't be null.");}
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

    /**
     * Get {@link Scale} level prefix for result.
     * if result=[0, minor_differences.value), then level = full_match,
     * if result=[minor_differences.value, moderate_differences.value) - then level = minor_differences,
     * if result=[moderate_differences.value, strong_differences)  - then level = moderate_differences,
     * if result=[strong_differences, total.number.of.questions) - then level = strong_differences
     * @param result must not be {@literal null}.
     * @param env must not be {@literal null}.
     * @return scale level prefix value.
     * @throws BadRequestException if result is {@literal null}.
     * @throws NullPointerException if env is {@literal null}.
     */
    private static String getScaleLevelPrefix(Integer result, Environment env) {
        if (result == null) {throw new BadRequestException("result can't be null.");}
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
