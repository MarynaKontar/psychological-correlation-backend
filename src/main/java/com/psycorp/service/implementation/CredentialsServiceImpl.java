package com.psycorp.service.implementation;

import com.psycorp.exception.AuthorizationException;
import com.psycorp.exception.BadRequestException;
import com.psycorp.model.entity.User;
import com.psycorp.model.enums.ErrorEnum;
import com.psycorp.model.enums.TokenType;
import com.psycorp.model.enums.UserRole;
import com.psycorp.model.objects.Credentials;
import com.psycorp.model.objects.UserAccount;
import com.psycorp.model.security.CredentialsEntity;
import com.psycorp.model.security.TokenEntity;
import com.psycorp.repository.UserRepository;
import com.psycorp.repository.security.CredentialsRepository;
import com.psycorp.security.token.TokenPrincipal;
import com.psycorp.service.CredentialsService;
import com.psycorp.service.UserAccountService;
import com.psycorp.service.UserService;
import com.psycorp.service.security.AuthService;
import com.psycorp.service.security.TokenService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.psycorp.security.SecurityConstant.ACCESS_TOKEN_PREFIX;


/**
 * Service implementation for CredentialsService.
 * @author Vitaliy Proskura
 * @author Maryna Kontar
 */

@Service
public class CredentialsServiceImpl implements CredentialsService{

    private final CredentialsRepository credentialsRepository;
    private final UserRepository userRepository;
    private final UserAccountService userAccountService;
    private final TokenService tokenService;
    private final UserService userService;
    private final MongoOperations mongoOperations;
    private final AuthService authService;
    private final Environment env;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public CredentialsServiceImpl(CredentialsRepository credentialsRepository,
                                  UserRepository userRepository,
                                  UserAccountService userAccountService,
                                  TokenService tokenService,
                                  UserService userService,
                                  MongoOperations mongoOperations,
                                  AuthService authService,
                                  Environment env,
                                  PasswordEncoder passwordEncoder) {
        this.credentialsRepository = credentialsRepository;
        this.userRepository = userRepository;
        this.userAccountService = userAccountService;
        this.tokenService = tokenService;
        this.userService = userService;
        this.mongoOperations = mongoOperations;
        this.authService = authService;
        this.env = env;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Saves credentials.
     * User, user account data and credentials are saved to the database.
     * Token type are changed to ACCESS if token exist.
     * @param credentials must not be {@literal null}.
     * @return user account for saved user.
     * @throws BadRequestException if user name or email already exists in database.
     */
    @Override
    @Transactional
    public UserAccount save(Credentials credentials){
        TokenPrincipal tokenPrincipal = (TokenPrincipal) authService.getAuthPrincipal();
        User user;
        if(tokenPrincipal != null && tokenPrincipal.getId() != null) { //if there is already user
            ObjectId principalId = tokenPrincipal.getId();

            //if user is registered by INVITE_TOKEN, change it to ACCESS_TOKEN
            TokenEntity tokenEntity = tokenService.findByUserId(principalId);
            if(tokenEntity.getToken() != null && tokenEntity.getType() == TokenType.INVITE_TOKEN) {
                tokenService.changeInviteTokenToAccess(ACCESS_TOKEN_PREFIX + " " + tokenEntity.getToken());
            }

            // and if there is user for token, than update this user
            if(userRepository.findById(principalId).isPresent()){
                user = update(credentials, principalId);
            } else {
                throw new BadRequestException(env.getProperty("error.noUserFound") + " for user id: " + principalId);
            }
        } else {// if token == null or tokenId == null, create new user
            user = insert(credentials);
        }

        return userAccountService.getUserAccount(user);
    }

    /**
     * Changes old password from the storage to newPassword.
     * Encoded oldPassword is verified with the encoded password obtained from storage
     * and if they are matched password form the storage is replaced with newPassword.
     * The stored password itself is never decoded.
     * @param oldPassword raw password to check if it is equal to the password obtained from storage.
     * @param newPassword new raw password, which will be saved instead of the old one from the storage.
     * @throws {@link org.springframework.security.authentication.BadCredentialsException}
     * if encoded oldPassword doesn't match to password obtained from storage.
     */
    @Override
    public void changePassword(String oldPassword, String newPassword) {
        User principal = userService.getPrincipalUser();
        CredentialsEntity credentialsEntity = findByUserId(principal.getId());
        if (!passwordEncoder.matches(oldPassword, credentialsEntity.getPassword())) {
            throw new AuthorizationException(env.getProperty("error.YouEnterWrongPassword"), ErrorEnum.PASSWORD_WRONG);
        }

        ObjectId credentialsId = credentialsEntity.getId();

        // UPDATE PASSWORD
        if(newPassword != null) {
            Update updateCredentials = new Update()
                    .set("password", passwordEncoder.encode(newPassword));

            Query queryCredentials = Query.query(Criteria.where(Fields.UNDERSCORE_ID).is(credentialsId));
            mongoOperations.findAndModify(queryCredentials, updateCredentials, CredentialsEntity.class);
        }
    }

    /**
     * Updates credentials for principal user: updates principal user info,
     * update password if it isn't {@literal null}, create user account if none exists.
     * @param credentials must not be {@literal null}.
     * @param principalId must not be {@literal null}.
     * @return updated user.
     * @throws NullPointerException if credentials or principalId is {@literal null}.
     * @throws BadRequestException if credential.getUser().getName() or credentials.getUser().getEmail() already exists in database.
     */
    private User update(Credentials credentials, ObjectId principalId){

        User principal = userService.findById(principalId);
        ObjectId credentialsId = findByUserId(principalId).getId();

        // CHECK NAME AND EMAIL ON UNIQUE
        if (credentials.getUser().getName() != null &&
                principal.getName() != null &&
                !principal.getName().equals(credentials.getUser().getName())) {
            userService.checkIfUsernameOrEmailExist(credentials.getUser().getName());
        }
        if (credentials.getUser().getEmail() != null &&
                principal.getEmail() != null &&
                !principal.getEmail().equals(credentials.getUser().getEmail())) {
            userService.checkIfUsernameOrEmailExist(credentials.getUser().getEmail());
        }

        // UPDATE USER
        if (credentials.getPassword() != null) {
            credentials.getUser().setRole(UserRole.USER);
        }
        User user = userService.updateUser(credentials.getUser());

        // UPDATE CredentialsEntity
        Update updateCredentials = new Update()
                .set("password", (credentials.getPassword() != null)?
                        passwordEncoder.encode(credentials.getPassword()): credentials.getPassword());

        Query queryCredentials = Query.query(Criteria.where(Fields.UNDERSCORE_ID).is(credentialsId));
        mongoOperations.findAndModify(queryCredentials, updateCredentials, CredentialsEntity.class);

        // CREATE USERACCOUNT
        if (userAccountService.getUserAccountEntityByUserIdOrNull(user.getId()) == null) {
            userAccountService.insert(user);
        }

        return user;
    }

    /**
     * Inserts new user, user account entity and credential entity to database.
     * @param credentials must not be {@literal null}.
     * @return inserted user.
     * @throws BadRequestException if credential.getUser().getName()
     * or credentials.getUser().getEmail() already exists in database.
     */
    private User insert(Credentials credentials){

        // CHECK NAME AND EMAIL ON UNIQUE
        if (credentials.getUser().getName() != null) {
            userService.checkIfUsernameOrEmailExist(credentials.getUser().getName());
        }
        if (credentials.getUser().getEmail() != null) {
            userService.checkIfUsernameOrEmailExist(credentials.getUser().getEmail());
        }

        if(credentials.getPassword() != null) {
            credentials.setPassword(passwordEncoder.encode(credentials.getPassword()));
            credentials.getUser().setRole(UserRole.USER);
        }

        User user = userRepository.insert(credentials.getUser());

        credentialsRepository.insert(createCredentialsEntity(credentials, user.getId()));

        userAccountService.insert(user);

        return user;
    }

    /**
     * Create new {@link CredentialsEntity} for user with userId.
     * @param credentials must not be {@literal null}.
     * @param userId must not be {@literal null}.
     * @return created {@link CredentialsEntity} for user with userId.
     */
    private CredentialsEntity createCredentialsEntity(Credentials credentials, ObjectId userId) {
        CredentialsEntity credentialsEntity = new CredentialsEntity();
        credentialsEntity.setUserId(userId);
        credentialsEntity.setPassword(credentials.getPassword());
        return credentialsEntity;
    }

    /**
     * Finds credentials entity by userId.
     * @param userId must not be {@literal null}.
     * @return credentials entity by userId.
     * @throws AuthorizationException if none found.
     */
    private CredentialsEntity findByUserId(ObjectId userId) {
        return credentialsRepository.findByUserId(userId).orElseThrow(() ->
                new AuthorizationException("Not found credentials for userId: " + userId, ErrorEnum.NOT_AUTHORIZED));
    }

}
