package com.psycorp.service;

import com.psycorp.model.entity.User;
import com.psycorp.model.objects.Credentials;
import com.psycorp.model.objects.UserAccount;

public interface CredentialsService {
    UserAccount save(Credentials credentials, String token);

    User changePassword(String oldPassword, String newPassword);
}
