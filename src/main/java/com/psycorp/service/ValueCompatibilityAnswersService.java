package com.psycorp.service;

import com.psycorp.exception.BadRequestException;
import com.psycorp.model.entity.*;
import com.psycorp.model.enums.Area;
import org.bson.types.ObjectId;

import java.util.List;

/**
 * Service interface for {@link ValueCompatibilityAnswersEntity}.
 * @author Maryna Kontar
 */
public interface ValueCompatibilityAnswersService {

    /**
     * Gets initial values for value compatibility test.
     * @return {@link ValueCompatibilityAnswersEntity} with initial values.
     */
    ValueCompatibilityAnswersEntity getInitValueCompatibilityAnswers();

    /**
     * Saves choices for given area for user that matches token or to principal user if token is {@literal null}.
     * @param token user token ()
     * @param userForMatchingToken token to add for matching.
     * @param choices must not be {@literal null}.
     * @param area must not be {@literal null}.
     * @return saved {@link ValueCompatibilityAnswersEntity}.
     */
    ValueCompatibilityAnswersEntity saveFirstPartOfTests(String token,
                                                         String userForMatchingToken,
                                                         List<Choice> choices,
                                                         Area area);

    /**
     * Saves choices for given area to last not passed {@link ValueCompatibilityAnswersEntity}
     * (or to new one, if all tests are passed)
     * for user taken by token (if the user visits the app using the link with the token)
     * or for principal user, if token is {@literal null}.
     * @param choices must not be {@literal null}.
     * @param area must not be {@literal null}.
     * @return saved {@link ValueCompatibilityAnswersEntity}.
     */
    ValueCompatibilityAnswersEntity saveChoices(String token,
                                                List<Choice> choices,
                                                Area area);

    /**
     * Gets last passed test for user.
     * @param user must not be {@literal null}.
     * @return last passed {@link ValueCompatibilityAnswersEntity} for given user.
     * @throws BadRequestException if user is {@literal null}
     * or if no passed {@link ValueCompatibilityAnswersEntity} is found.
     */
    ValueCompatibilityAnswersEntity getLastPassedTest(User user);

    /**
     * Returns whether a value compatibility test passed for user with userId.
     * @param userId must not be {@literal null}.
     * @return {@literal true} if a value compatibility test passed
     * for user with userId, {@literal false} otherwise.
     * @throws IllegalArgumentException if {@code userId} is {@literal null}.
     */
    Boolean ifTestPassed(ObjectId userId);
}
