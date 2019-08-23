package com.psycorp.service;

import com.psycorp.model.entity.User;
import com.psycorp.model.objects.ValueProfileIndividual;
import com.psycorp.model.objects.ValueProfileMatching;
import org.bson.types.ObjectId;

/**
 * Service interface for {@link ValueProfileIndividual} and {@link ValueProfileMatching}.
 * @author Maryna Kontar
 */
public interface ValueProfileService {

    /**
     * Returns {@link ValueProfileIndividual} with comments for noPrincipalUser
     * if it isn't {@literal null} or for principal user if it is.
     * @param noPrincipalUser equals {@literal null} for principal user.
     * @return {@link ValueProfileIndividual}.
     */
    ValueProfileIndividual getValueProfileIndividual(User noPrincipalUser);

    /**
     * Returns {@link ValueProfileMatching} with comments for  user with noPrincipalUserId and principal user.
     * @param noPrincipalUserId must not be {@literal null}.
     * @return {@link ValueProfileMatching}.
     */
    ValueProfileMatching getValueProfileForMatching(ObjectId noPrincipalUserId);
}
