package com.psycorp.service.implementation;

import com.psycorp.exception.AuthorizationException;
import com.psycorp.exception.BadRequestException;
import com.psycorp.model.entity.User;
import com.psycorp.model.enums.ErrorEnum;
import com.psycorp.model.enums.UserRole;
import com.psycorp.model.security.CredentialsEntity;
import com.psycorp.repository.UserAnswersRepository;
import com.psycorp.repository.UserMatchRepository;
import com.psycorp.repository.UserRepository;
import com.psycorp.repository.security.CredentialsRepository;
import com.psycorp.repository.security.TokenRepository;
import com.psycorp.security.token.TokenPrincipal;
import com.psycorp.service.security.AuthService;
import com.psycorp.util.AuthUtil;
import com.psycorp.service.UserService;
import org.bson.types.ObjectId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
//@Primary
//@Scope("singleton")//default бины создаются синглтонами
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CredentialsRepository credentialsRepository;
    private final UserAnswersRepository userAnswersRepository;
    private final UserMatchRepository userMatchRepository;
    @Autowired
    private AuthService authService;
    private final TokenRepository tokenRepository;
    private final MongoOperations mongoOperations;
    private final AuthUtil authUtil;
    private final Environment env;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, CredentialsRepository credentialsRepository, UserAnswersRepository userAnswersRepository
            , UserMatchRepository userMatchRepository, TokenRepository tokenRepository, MongoOperations mongoOperations, AuthUtil serviceUtil, Environment env) {
        this.userRepository = userRepository;
        this.credentialsRepository = credentialsRepository;
        this.userAnswersRepository = userAnswersRepository;
        this.userMatchRepository = userMatchRepository;
        this.tokenRepository = tokenRepository;
        this.mongoOperations = mongoOperations;
        this.authUtil = serviceUtil;
        this.env = env;
    }

//    @Override
//    public User createUser(User user) {
//        return userRepository.insert(user);
//    }

    @Override
    @Transactional
    public User createAnonimUser() {
        User user;
        user = userRepository.save(new User(UUID.randomUUID().toString(), UserRole.ANONIM));
        CredentialsEntity credentialsEntity = new CredentialsEntity();
        credentialsEntity.setUser(user);
        credentialsRepository.save(credentialsEntity);
        return user;
    }

    @Override
    public User addAgeAndGender(User user) {
        User principal = getPrincipalUser();
        Update updateUser = new Update()
                .set("age", user.getAge())
                .set("gender", user.getGender());

        Query queryUser = Query.query(Criteria.where(Fields.UNDERSCORE_ID).is(principal.getId()));
        user = mongoOperations.findAndModify(queryUser, updateUser
                , new FindAndModifyOptions().returnNew(true), User.class);
        return user;
    }

    @Override
    public User find(User user) {
        if (user != null && user.getId() != null) {
            user = findById(user.getId());
        } else {
            throw new BadRequestException(env.getProperty("error.UserOrUserIdCan`tBeNull"));
        }
        return user;
    }

    @Override
    public User findById(ObjectId userId) {
//        authUtil.userAuthorization(userId);
//        userRepository.existsById(userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException(env.getProperty("error.noUserFound") + " for user id: " + userId));
    }

    @Override
    public User findUserByNameOrEmail(String nameOrEmail) {
        return userRepository.findUserByNameOrEmail(nameOrEmail, nameOrEmail)
                .orElseThrow(() -> new BadRequestException(env.getProperty("error.noUserFound") + " for name or email: "
                        + nameOrEmail));
    }

    @Override
    public User findFirstUserByName(String name) {
        return userRepository.findFirstByName(name)
                .orElseThrow(() -> new BadRequestException(env.getProperty("error.noUserFound") + " for user name: " + name));
    }

    @Override
    public User getPrincipalUser() {

        TokenPrincipal tokenPrincipal = (TokenPrincipal) authService.getAuthPrincipal();
        if(tokenPrincipal != null && tokenPrincipal.getId() != null) { //если есть токен
            return findById(tokenPrincipal.getId()); // и для него есть пользователь, то берем этого пользователя
        } else { throw new AuthorizationException("User not authorised", ErrorEnum.NOT_AUTHORIZED); } // если токен == null или у него id == null
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User updateUser(User user) {
        User principal = getPrincipalUser();
        Update updateUser = new Update();
        if(user.getName() != null) { updateUser.set("name", user.getName()); }
        if(user.getAge() != null) { updateUser.set("age", user.getAge()); }
        if(user.getGender() != null) { updateUser.set("gender", user.getGender()); }
        if(user.getEmail() != null) { updateUser.set("email", user.getEmail()); }

        Query queryUser = Query.query(Criteria.where(Fields.UNDERSCORE_ID).is(principal.getId()));
        user = mongoOperations.findAndModify(queryUser, updateUser
                , new FindAndModifyOptions().returnNew(true), User.class);
        return user;
//        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User deleteUser(ObjectId userId) {

//        authUtil.userAuthorization(userId);

        User user = findById(userId);
        userAnswersRepository.removeAllByUserId(userId);
        userMatchRepository.removeAllByUserId(userId);
        tokenRepository.removeAllByUserId(userId);
        //remove user from usersForMatching of another users
        userRepository.delete(user);
        return user;
    }


}
