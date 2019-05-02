package com.psycorp.service;

import com.psycorp.model.entity.User;
import com.psycorp.model.security.CredentialsEntity;

public interface CredentialsService {
    User save(CredentialsEntity credentialsEntity, String token);

    User changePassword(CredentialsEntity credentialsEntity);
}
