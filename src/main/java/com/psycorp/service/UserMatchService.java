package com.psycorp.service;

import com.psycorp.model.entity.UserMatchEntity;
import com.psycorp.model.enums.MatchMethod;

import java.util.List;

public interface UserMatchService extends Match{

    UserMatchEntity insert(UserMatchEntity userMatchEntity);

    List<UserMatchEntity> findByUserName(String userName);

    List<UserMatchEntity> findByMatchMethod(MatchMethod matchMethod);

    List<UserMatchEntity> findByUserNameAndMatchMethod(String userName, MatchMethod matchMethod);

    List<UserMatchEntity> getAll();

}
