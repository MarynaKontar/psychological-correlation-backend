package com.psycorp.service;

import com.psycorp.model.entity.User;
import com.psycorp.model.objects.Credentials;

public interface CredentialsService {
    User save(Credentials credentials, String token);

    User changePassword(Credentials credentials);
}
