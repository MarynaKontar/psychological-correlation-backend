package com.psycorp.service;

import com.psycorp.exception.BadRequestException;
import com.psycorp.model.objects.Credentials;
import com.psycorp.model.objects.UserAccount;

/**
 * Service interface for {@link com.psycorp.model.security.CredentialsEntity} and {@link Credentials}.
 * @author Vitaliy Proskura
 * @author  Maryna Kontar
 */
public interface CredentialsService {

    /**
     * Save credentials.
     * User, user account data and credentials are saved to the database.
     * Token type are changed to ACCESS if token exist.
     * @param credentials must not be {@literal null}.
     * @return user account for saved user.
     * @throws BadRequestException if user name or email already exists in database.
     */
    UserAccount save(Credentials credentials);

    /**
     * Change old password from the storage to newPassword.
     * Encoded oldPassword is verified with the encoded password obtained from storage
     * and if they are matched password form the storage is replaced with newPassword.
     * The stored password itself is never decoded.
     * @param oldPassword raw password to check if it is equal to the password obtained from storage.
     * @param newPassword new raw password, which will be saved instead of the old one from the storage.
     * @throws {@link org.springframework.security.authentication.BadCredentialsException}
     * if encoded oldPassword doesn't match to password obtained from storage.
     */
    void changePassword(String oldPassword, String newPassword);
}
