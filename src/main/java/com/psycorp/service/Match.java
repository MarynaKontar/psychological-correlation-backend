package com.psycorp.service;

import com.psycorp.model.entity.User;
import com.psycorp.model.enums.MatchMethod;
import com.psycorp.model.objects.UserMatch;

/**
 * Service interface matching user test results.
 * @author Maryna Kontar
 */
public interface Match {

    /**
     * Matches results of tests for user and principal user by matchMethod.
     * @param user must not be {@literal null}.
     * @param matchMethod must not be {@literal null}.
     * @return {@link UserMatch}.
     */
    UserMatch match(User user, MatchMethod matchMethod);
}
