package com.psycorp.service;

import com.psycorp.model.entity.UserMatch;
import com.psycorp.model.enums.MatchMethod;

import java.util.List;

public interface UserMatchService extends Match{

    UserMatch insert(UserMatch userMatch);

    List<UserMatch> findByUserName(String userName);

    List<UserMatch> findByMatchMethod(MatchMethod matchMethod);

    List<UserMatch> findByUserNameAndMatchMethod(String userName, MatchMethod matchMethod);

    List<UserMatch> getAll();



}
