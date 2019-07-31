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
    public CredentialsServiceImpl(CredentialsRepository credentialsRepository, UserRepository userRepository,
                                  UserAccountService userAccountService, TokenService tokenService, UserService userService,
                                  MongoOperations mongoOperations, AuthService authService,
                                  Environment env, PasswordEncoder passwordEncoder) {
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

    @Override
    @Transactional
    public UserAccount save(Credentials credentials, String token){
        //TODO продумать, когда может приходить токен; пока его не использую
        TokenPrincipal tokenPrincipal = (TokenPrincipal) authService.getAuthPrincipal();
        User user;
        if(tokenPrincipal != null && tokenPrincipal.getId() != null) { //если есть пользователь
            ObjectId principalId = tokenPrincipal.getId();

            //если регистрируется пользователь по INVITE_TOKEN, то меняем его на ACCESS_TOKEN
            TokenEntity tokenEntity = tokenService.findByUserId(principalId);
            if(tokenEntity.getToken() != null && tokenEntity.getType() == TokenType.INVITE_TOKEN) {
                tokenService.changeInviteTokenToAccess(tokenEntity.getToken());
            }

            if(userRepository.findById(principalId).isPresent()){ // и для него есть пользователь, то берем этого пользователя
                user = update(credentials, principalId);
            } else {
                throw new BadRequestException(env.getProperty("error.noUserFound") + " for user id: " + principalId);
            }
        } else {
            // если токен == null или у него id == null, то создаем нового пользователя
            user = insert(credentials);
        }

        return userAccountService.getUserAccount(user);
    }

    @Override
    public User changePassword(String oldPassword, String newPassword) {
        User principal = userService.getPrincipalUser();
        CredentialsEntity credentialsEntity = findByUserId(principal.getId());
        if (!passwordEncoder.matches(oldPassword, credentialsEntity.getPassword())) {
            throw new AuthorizationException("You entered the wrong password. ", ErrorEnum.PASSWORD_WRONG);
        }

        ObjectId credentialsId = credentialsEntity.getId();

        // UPDATE PASSWORD
        if(newPassword != null) {
            Update updateCredentials = new Update()
                    .set("password", passwordEncoder.encode(newPassword));

            Query queryCredentials = Query.query(Criteria.where(Fields.UNDERSCORE_ID).is(credentialsId));
            mongoOperations.findAndModify(queryCredentials, updateCredentials, CredentialsEntity.class);
        }
        return new User();
    }

    private User update(Credentials credentials, ObjectId userId){

        User principal = userService.findById(userId);
        ObjectId credentialsId = findByUserId(userId).getId();

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

    private CredentialsEntity createCredentialsEntity(Credentials credentials, ObjectId userId) {
        CredentialsEntity credentialsEntity = new CredentialsEntity();
        credentialsEntity.setUserId(userId);
        credentialsEntity.setPassword(credentials.getPassword());
        return credentialsEntity;
    }

    private CredentialsEntity findByUserId(ObjectId userId) {
        return credentialsRepository.findByUserId(userId).orElseThrow(() ->
                new AuthorizationException("not found credentials for userId: " + userId, ErrorEnum.NOT_AUTHORIZED));
    }

}
