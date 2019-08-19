package com.psycorp.service;

import com.psycorp.model.entity.User;
import com.psycorp.model.objects.ValueProfileIndividual;
import com.psycorp.model.objects.ValueProfileMatching;
import org.bson.types.ObjectId;

/**
 * Service interface for {@link ValueProfileIndividual} and {@link ValueProfileMatching}.
 * @author  Maryna Kontar
 */
public interface ValueProfileService {

    ValueProfileIndividual getValueProfileIndividual(User noPrincipalUser);

    ValueProfileMatching getValueProfileForMatching(ObjectId noPrincipalUserId);
}
