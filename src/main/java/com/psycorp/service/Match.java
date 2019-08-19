package com.psycorp.service;

import com.psycorp.model.entity.User;
import com.psycorp.model.enums.MatchMethod;
import com.psycorp.model.objects.UserMatch;

/**
 * Service interface matching user test results.
 * @author  Maryna Kontar
 */
public interface Match {

    UserMatch match(User user, MatchMethod matchMethod);
}
