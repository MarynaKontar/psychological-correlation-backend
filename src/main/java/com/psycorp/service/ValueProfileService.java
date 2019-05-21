package com.psycorp.service;

import com.psycorp.model.entity.User;
import com.psycorp.model.objects.ValueProfile;
import com.psycorp.model.objects.ValueProfileIndividual;
import com.psycorp.model.objects.ValueProfileMatching;
import org.bson.types.ObjectId;

public interface ValueProfileService {

    ValueProfileIndividual getValueProfileIndividual(User noPrincipalUser);

    ValueProfileMatching getValueProfileForMatching(ObjectId noPrincipalUserId);
//    ValueProfile getValueProfileIndividual(User noPrincipalUser);
//
//    ValueProfileMatching getValueProfileForMatching(User noPrincipalUser);
}
