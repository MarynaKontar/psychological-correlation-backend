package com.psycorp.service;

import com.psycorp.model.entity.User;
import com.psycorp.model.enums.MatchMethod;
import com.psycorp.model.objects.UserMatch;

public interface Match {

    /**
     * Create UserMatchEntity with fields userOne = user1, userTwo = user2
     * and totalMatch, goalMatch, qualityMatch, stateMatch that calculate by matchMethod (Percent or Pearson association coefficient
     * @param user1
     * @param user2
     * @return UserMatchEntity
     */
//    UserMatchEntity match(User user1, User user2, MatchMethod matchMethod);
//    UserMatchEntity match(User user, MatchMethod matchMethod);
    UserMatch match(User user1, User user2, MatchMethod matchMethod);
    UserMatch match(User user, MatchMethod matchMethod);
}
