package com.psycorp.util;

import com.psycorp.model.entity.Match;
import com.psycorp.model.entity.UserMatch;
import com.psycorp.model.enums.Area;
import com.psycorp.model.enums.MatchMethod;

public class Advice {

    public static String getAdvice(UserMatch userMatch){
        StringBuilder advice = new StringBuilder("");

        for (Match match : userMatch.getMatches()) {
            if(match.getArea().equals(Area.GOAL) && match.getMatchMethod().equals(MatchMethod.PEARSONCORRELATION)
//            && match.getResult() >= 0.7){
            && match.getResult().getNumber() >= 0.7){
                advice.append(Area.GOAL.toString()).append(" ")
                        .append(MatchMethod.PEARSONCORRELATION).append("....СОВПАДАЕТЕ  ");
            } else advice.append(Area.GOAL.toString()).append(" ")
                    .append(MatchMethod.PEARSONCORRELATION).append("....НЕ СОВПАДАЕТЕ  ");
        }

        for (Match match : userMatch.getMatches()) {
            if(match.getArea().equals(Area.QUALITY) && match.getMatchMethod().equals(MatchMethod.PEARSONCORRELATION)
//                    && match.getResult() >= 0.7){
                    && match.getResult().getNumber() >= 0.7){
                advice.append(Area.QUALITY.toString()).append(" ")
                        .append(MatchMethod.PEARSONCORRELATION).append("....СОВПАДАЕТЕ  ");
            } else advice.append(Area.QUALITY.toString()).append(" ")
                    .append(MatchMethod.PEARSONCORRELATION).append("....НЕ СОВПАДАЕТЕ  ");
        }

        for (Match match : userMatch.getMatches()) {
            if(match.getArea().equals(Area.STATE) && match.getMatchMethod().equals(MatchMethod.PEARSONCORRELATION)
//                    && match.getResult() >= 0.7){
                    && match.getResult().getNumber() >= 0.7){
                advice.append(Area.STATE.toString()).append(" ")
                        .append(MatchMethod.PEARSONCORRELATION).append("....СОВПАДАЕТЕ  ");
            } else advice.append(Area.STATE.toString()).append(" ")
                    .append(MatchMethod.PEARSONCORRELATION).append("....НЕ СОВПАДАЕТЕ  ");
        }


        return "There is no one advice was written yet " + advice;
    }
}
