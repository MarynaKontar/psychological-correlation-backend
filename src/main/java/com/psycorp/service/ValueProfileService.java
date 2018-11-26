package com.psycorp.service;

import com.psycorp.model.entity.User;
import com.psycorp.model.objects.ValueProfile;
import com.psycorp.model.objects.ValueProfileIndividual;
import com.psycorp.model.objects.ValueProfileMatching;

public interface ValueProfileService {

    ValueProfileIndividual getValueProfileIndividual(User noPrincipalUser);

    ValueProfileMatching getValueProfileForMatching(User noPrincipalUser);
//    ValueProfile getValueProfileIndividual(User noPrincipalUser);
//
//    ValueProfileMatching getValueProfileForMatching(User noPrincipalUser);
}
