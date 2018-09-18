package com.psycorp.service.implementation;

import com.psycorp.exception.AuthorizationException;
import com.psycorp.model.entity.User;
import com.psycorp.model.enums.ErrorEnum;
import com.psycorp.model.enums.UserRole;
import com.psycorp.model.security.CredentialsEntity;
import com.psycorp.repository.UserRepository;
import com.psycorp.repository.security.CredentialsRepository;
import com.psycorp.security.token.TokenPrincipal;
import com.psycorp.service.CredentialsService;
import com.psycorp.service.security.AuthService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final MongoOperations mongoOperations;
    private final AuthService authService;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public CredentialsServiceImpl(CredentialsRepository credentialsRepository, UserRepository userRepository,
                                  MongoOperations mongoOperations, AuthService authService,
                                  PasswordEncoder passwordEncoder) {
        this.credentialsRepository = credentialsRepository;
        this.userRepository = userRepository;
        this.mongoOperations = mongoOperations;
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User save(CredentialsEntity credentialsEntity){

        TokenPrincipal tokenPrincipal = (TokenPrincipal) authService.getAuthPrincipal();
        User user;
        if(tokenPrincipal != null && tokenPrincipal.getId() != null) { //если есть токен
            if(userRepository.findById(tokenPrincipal.getId()).isPresent()){ // и для него есть пользователь, то берем этого пользователя
                user = update(credentialsEntity, tokenPrincipal.getId());
            } else {
                user = insert(credentialsEntity);
            }
        } else {
            user = insert(credentialsEntity);

        } // если токен == null или у него id == null, то создаем нового пользователя


       return user;
    }

    private User update(CredentialsEntity credentialsEntity, ObjectId userId){
        //TODO dbref в tokenEntity сделать и пересохранить тесты

        ObjectId credentialsId = credentialsRepository.findByUser_Id(userId).orElseThrow(() ->
                new AuthorizationException("", ErrorEnum.UNKNOWN_ERROR)).getId();

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

}
