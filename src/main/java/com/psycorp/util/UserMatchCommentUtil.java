package com.psycorp.util;

import com.psycorp.model.entity.Match;
import com.psycorp.model.entity.UserMatch;
import com.psycorp.model.entity.UserMatchComment;
import com.psycorp.model.enums.Area;
import com.psycorp.model.enums.MatchMethod;
import org.springframework.core.env.Environment;

/**
 * Util class for getting comment on user match for some Area or Scale
 */
public class UserMatchCommentUtil {

    public static UserMatchComment getComment(Match match, Environment env){

        UserMatchComment userMatchComment = new UserMatchComment();
        userMatchComment.setAspect(match.getArea());
        userMatchComment.setAspectDescription("ASPECTDESCRIPTION");
        userMatchComment.setHeader("HEADER");
        userMatchComment.setForeword("FOREWORD");
        userMatchComment.setAccent("ACCENT");
        userMatchComment.setMainText("MAIN TEXT");
        userMatchComment.setResult(match.getResult().getNumber());
        userMatchComment.setLevel("LEVEL");

//        StringBuilder advice = new StringBuilder("");
//        for (Match match : match1.getMatches()) {
//            if(match.getArea().equals(Area.GOAL) && match.getMatchMethod().equals(MatchMethod.PEARSONCORRELATION)
//            && match.getResult().getNumber() >= 0.7){
//                advice.append(Area.GOAL.toString()).append(" ")
//                        .append(MatchMethod.PEARSONCORRELATION).append("....СОВПАДАЕТЕ  ");
//            } else advice.append(Area.GOAL.toString()).append(" ")
//                    .append(MatchMethod.PEARSONCORRELATION).append("....НЕ СОВПАДАЕТЕ  ");
//        }
//
//        for (Match match : match1.getMatches()) {
//            if(match.getArea().equals(Area.QUALITY) && match.getMatchMethod().equals(MatchMethod.PEARSONCORRELATION)
//                    && match.getResult().getNumber() >= 0.7){
//                advice.append(Area.QUALITY.toString()).append(" ")
//                        .append(MatchMethod.PEARSONCORRELATION).append("....СОВПАДАЕТЕ  ");
//            } else advice.append(Area.QUALITY.toString()).append(" ")
//                    .append(MatchMethod.PEARSONCORRELATION).append("....НЕ СОВПАДАЕТЕ  ");
//        }
//
//        for (Match match : match1.getMatches()) {
//            if(match.getArea().equals(Area.STATE) && match.getMatchMethod().equals(MatchMethod.PEARSONCORRELATION)
//                    && match.getResult().getNumber() >= 0.7){
//                advice.append(Area.STATE.toString()).append(" ")
//                        .append(MatchMethod.PEARSONCORRELATION).append("....СОВПАДАЕТЕ  ");
//            } else advice.append(Area.STATE.toString()).append(" ")
//                    .append(MatchMethod.PEARSONCORRELATION).append("....НЕ СОВПАДАЕТЕ  ");
//        }


//        return "There is no one advice was written yet " + advice;
        return userMatchComment;
    }
}
