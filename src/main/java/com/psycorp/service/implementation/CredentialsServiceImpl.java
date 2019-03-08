package com.psycorp.service.implementation;

import com.psycorp.exception.AuthorizationException;
import com.psycorp.exception.BadRequestException;
import com.psycorp.model.entity.User;
import com.psycorp.model.enums.ErrorEnum;
import com.psycorp.model.enums.UserRole;
import com.psycorp.model.security.CredentialsEntity;
import com.psycorp.repository.UserRepository;
import com.psycorp.repository.security.CredentialsRepository;
import com.psycorp.security.token.TokenPrincipal;
import com.psycorp.service.CredentialsService;
import com.psycorp.service.UserService;
import com.psycorp.service.security.AuthService;
import com.psycorp.service.security.TokenService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
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
    private final TokenService tokenService;
    private final UserService userService;
    private final MongoOperations mongoOperations;
    private final AuthService authService;
    private final Environment env;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public CredentialsServiceImpl(CredentialsRepository credentialsRepository, UserRepository userRepository,
                                  TokenService tokenService, UserService userService,
                                  MongoOperations mongoOperations, AuthService authService,
                                  Environment env, PasswordEncoder passwordEncoder) {
        this.credentialsRepository = credentialsRepository;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.userService = userService;
        this.mongoOperations = mongoOperations;
        this.authService = authService;
        this.env = env;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User save(CredentialsEntity credentialsEntity){

        TokenPrincipal tokenPrincipal = (TokenPrincipal) authService.getAuthPrincipal();
        User user;
        if(tokenPrincipal != null && tokenPrincipal.getId() != null) { //если есть пользователь
            ObjectId principalId = tokenPrincipal.getId();







//            if(token != null) {tokenService.changeInviteTokenToAccess(token); }
//            tokenService.findByUserId(principalId).getToken()
            if(userRepository.findById(principalId).isPresent()){ // и для него есть пользователь, то берем этого пользователя
                user = update(credentialsEntity, principalId);
            } else {
//                user = insert(credentialsEntity);
                throw new BadRequestException(env.getProperty("error.noUserFound") + " for user id: " + principalId);
            }
        } else {
            user = insert(credentialsEntity);

        } // если токен == null или у него id == null, то создаем нового пользователя

       return user;
    }

    @Override
    public User changePassword(CredentialsEntity credentialsEntity) {
        User principal = userService.getPrincipalUser();
        ObjectId credentialsId = findByUserId(principal.getId()).getId();

        // UPDATE PASSWORD
        if(credentialsEntity.getPassword() != null) {
            Update updateCredentials = new Update()
                    .set("password", passwordEncoder.encode(credentialsEntity.getPassword()));

            Query queryCredentials = Query.query(Criteria.where(Fields.UNDERSCORE_ID).is(credentialsId));
            mongoOperations.findAndModify(queryCredentials, updateCredentials, CredentialsEntity.class);
        }
        return new User();
    }

    private User update(CredentialsEntity credentialsEntity, ObjectId userId){
        //TODO dbref в tokenEntity сделать и пересохранить тесты

        ObjectId credentialsId = findByUserId(userId).getId();

        // UPDATE USER
        Update updateUser = new Update()
                .set("name", credentialsEntity.getUser().getName())
                .set("email", credentialsEntity.getUser().getEmail())
                .set("role", (credentialsEntity.getPassword() != null)?
                        UserRole.USER : credentialsEntity.getUser().getRole());

        Query queryUser = Query.query(Criteria.where(Fields.UNDERSCORE_ID).is(userId));
        User user = mongoOperations.findAndModify(queryUser, updateUser
                , new FindAndModifyOptions().returnNew(true), User.class);


        // UPDATE CredentialsEntity
        Update updateCredentials = new Update()
                .set("user", user)
                .set("password", (credentialsEntity.getPassword() != null)?
                        passwordEncoder.encode(credentialsEntity.getPassword()): credentialsEntity.getPassword());

        Query queryCredentials = Query.query(Criteria.where(Fields.UNDERSCORE_ID).is(credentialsId));
        mongoOperations.findAndModify(queryCredentials, updateCredentials, CredentialsEntity.class);

        return user;
    }

    private User insert(CredentialsEntity credentialsEntity){
        if(credentialsEntity.getPassword() != null) {
            credentialsEntity.setPassword(passwordEncoder.encode(credentialsEntity.getPassword()));
            credentialsEntity.getUser().setRole(UserRole.USER);
        }

        User user = userRepository.insert(credentialsEntity.getUser());
        credentialsEntity.setUser(user);

        credentialsEntity = credentialsRepository.insert(credentialsEntity);
        return user;
    }

    private CredentialsEntity findByUserId(ObjectId userId) {
        return credentialsRepository.findByUser_Id(userId).orElseThrow(() ->
                new AuthorizationException("", ErrorEnum.UNKNOWN_ERROR));
    }

}
