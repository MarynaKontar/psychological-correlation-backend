package com.psycorp.service;

import com.psycorp.model.entity.User;
import com.psycorp.model.entity.UserMatch;

public interface UserMatchService {

    UserMatch insert(UserMatch userAnswers);

    String goalMatch(User user1, User user2);

    String totalMatch(User user1, User user2);

    String qualityMatch(User user1, User user2);

    String stateMatch(User user1, User user2);
}
