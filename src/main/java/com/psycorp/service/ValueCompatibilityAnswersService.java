package com.psycorp.service;

import com.psycorp.model.entity.*;
import com.psycorp.model.enums.Area;
import org.bson.types.ObjectId;

import java.util.List;

/**
 * Service interface for {@link ValueCompatibilityAnswersEntity}.
 * @author  Maryna Kontar
 */
public interface ValueCompatibilityAnswersService {

    ValueCompatibilityAnswersEntity getInitValueCompatibilityAnswers();
    ValueCompatibilityAnswersEntity saveFirstPartOfTests(String token,
                                                         String userForMatchingToken,
                                                         ValueCompatibilityAnswersEntity answersEntity,
                                                         List<Choice> choices,
                                                         Area area);

    ValueCompatibilityAnswersEntity saveChoices(String token,
                                                String userForMatchingToken,
                                                ValueCompatibilityAnswersEntity valueCompatibilityAnswersEntity,
                                                List<Choice> choices,
                                                Area area);


    ValueCompatibilityAnswersEntity getLastPassedTest(User user);
    Boolean ifTestPassed(ObjectId userId);
}
