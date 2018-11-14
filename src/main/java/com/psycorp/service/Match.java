package com.psycorp.service;

import com.psycorp.model.entity.User;
import com.psycorp.model.entity.UserMatch;
import com.psycorp.model.enums.MatchMethod;

public interface Match {

    /**
     * Create UserMatch with fields userOne = user1, userTwo = user2
     * and totalMatch, goalMatch, qualityMatch, stateMatch that calculate by matchMethod (Percent or Pearson association coefficient
     * @param user1
     * @param user2
     * @return UserMatch
     */
    UserMatch match(User user1, User user2, MatchMethod matchMethod);
    UserMatch match(User user, MatchMethod matchMethod);
}
