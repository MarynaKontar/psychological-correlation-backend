package com.psycorp.util;

import com.psycorp.model.objects.ValueProfileComment;
import com.psycorp.model.enums.Scale;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Util class for getting comment on user value profile for some Scale
 */
public class ValueProfileCommentUtil {

    /**
     * Return ValueProfileComment associated with the given scale and value
     * @param env
     * @param scale
     * @param value percent of chosen scale in value profile
     * @return
     */
    public static ValueProfileComment getComment(Environment env, @NotNull Scale scale, Double value){
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
//        comment.setResult(value);
//        comment.setFooter(env.getProperty(prefix + ".footer"));
        return comment;
    }

    /**
     * Return significance associated with the given value
     * @param env
     * @param value percent of chosen Scale in value profile
     * @return
     */
    private static String getSignificance(Environment env, Double value) {
        // если % (value) от 0 до low.value, то значимость (significance) = low,
        // если от low.value до average.value - то significance = average,
        // если больше average.value - то significance = high
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
