package com.psycorp.service;

import com.psycorp.model.entity.UserMatchEntity;
import com.psycorp.model.enums.MatchMethod;
import com.psycorp.model.objects.UserMatch;

import java.util.List;

public interface UserMatchService extends Match{

    UserMatchEntity insert(UserMatchEntity userMatchEntity);

    List<UserMatchEntity> findByMatchMethod(MatchMethod matchMethod);

    List<UserMatch> getAll();

}
