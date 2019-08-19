package com.psycorp.util;

import com.psycorp.exception.BadRequestException;
import com.psycorp.model.objects.ValueProfileComment;
import com.psycorp.model.enums.Scale;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * Util class for getting comment on user value profile for some Scale.
 */
public class ValueProfileCommentUtil {

    /**
     * Get {@link ValueProfileComment} depending on scale and value.
     * @param env must not be {@literal null}.
     * @param scale must not be {@literal null}.
     * @param value percent of chosen scale in value profile, must not be {@literal null}.
     * @return value profile comment.
     * @throws BadRequestException if scale or value are {@literal null}.
     * @throws NullPointerException if env is {@literal null}.
     * @throws IllegalArgumentException if value > 100.
     */
    public static ValueProfileComment getComment(Environment env, Scale scale, Double value){
        if (scale == null || value == null) {throw new BadRequestException("Scale and value can't be null.");}
        Assert.isTrue(value <= 100, "Value is a percent of chosen scale and can't be > 100.");
        String significance = getSignificance(env, value);

        String prefix = scale.name().toLowerCase() + "." + significance;

        ValueProfileComment comment = new ValueProfileComment();
        comment.setHead(env.getProperty(prefix + ".head"));
        comment.setHeader(env.getProperty(prefix + ".header"));
        int i = 1;
        List<String> list = new ArrayList<>();
        while(env.containsProperty(prefix + ".li." + i)){
            list.add(env.getProperty(prefix + ".li." + i));
            i = i + 1;
        }
        comment.setScale(env.getProperty(scale.name()));
        comment.setResult(value);
        comment.setList(list);
        comment.setFooter(env.getProperty(prefix + ".footer"));
        return comment;
    }

    /**
     * Get significance (значимость) prefix depending on value.
     * If value from 0 to low.value, then significance = low,
     * from low.value to average.value - then significance = average,
     * if more then average.value - then significance = high.
     * @param env must not be {@literal null}.
     * @param value must not be {@literal null}.
     * @return significance prefix.
     * @throws BadRequestException if value are {@literal null}.
     * @throws NullPointerException if env is {@literal null}.
     */
    private static String getSignificance(Environment env, Double value) {
        if (value == null) {throw new BadRequestException("Value can't be null.");} // not achievable
        String significance;
        Integer i = Integer.valueOf(env.getProperty("low.value"));
        Boolean v = value < Integer.valueOf(env.getProperty("low.value"));
        if (value < Integer.valueOf(env.getProperty("low.value"))) {
            significance = env.getProperty("low.level");
        } else if(value < Integer.valueOf(env.getProperty("average.value"))) {
            significance = env.getProperty("average.level");
        } else {
            significance = env.getProperty("high.level");
        }
        return significance;
    }
}
