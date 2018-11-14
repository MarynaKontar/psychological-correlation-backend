package com.psycorp.service.implementation;

import com.psycorp.exception.BadRequestException;
import com.psycorp.model.entity.User;
import com.psycorp.model.enums.UserRole;
import com.psycorp.model.security.CredentialsEntity;
import com.psycorp.repository.UserAnswersRepository;
import com.psycorp.repository.UserMatchRepository;
import com.psycorp.repository.UserRepository;
import com.psycorp.repository.security.CredentialsRepository;
import com.psycorp.security.token.TokenPrincipal;
import com.psycorp.service.security.AuthService;
import com.psycorp.util.AuthUtil;
import com.psycorp.service.UserService;
import org.bson.types.ObjectId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
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
    private final AuthService authService;
    private final AuthUtil authUtil;
    private final Environment env;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, CredentialsRepository credentialsRepository, UserAnswersRepository userAnswersRepository
            , UserMatchRepository userMatchRepository, AuthService authService, AuthUtil serviceUtil, Environment env) {
        this.userRepository = userRepository;
        this.credentialsRepository = credentialsRepository;
        this.userAnswersRepository = userAnswersRepository;
        this.userMatchRepository = userMatchRepository;
        this.authService = authService;
        this.authUtil = serviceUtil;
        this.env = env;
    }

    //TODO добавить проверку всех значений и соответствуюшии им Exceptions
    @Override
    public User createUser(User user) {
        return userRepository.insert(user);
    }

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
    public User findById(ObjectId userId) {
//        authUtil.userAuthorization(userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException(env.getProperty("error.noUserFound") + " for user id: " + userId));
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public User deleteUser(ObjectId userId) {

        authUtil.userAuthorization(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException(env.getProperty("error.noUserFound") + " for user id: " + userId));
        userAnswersRepository.removeAllByUserId(userId);
        userMatchRepository.removeAllByUserId(userId);
        userRepository.delete(user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User getPrincipalUser() {

        User user;
        TokenPrincipal tokenPrincipal = (TokenPrincipal) authService.getAuthPrincipal();

        if(tokenPrincipal != null && tokenPrincipal.getId() != null) { //если есть токен
            if(userRepository.findById(tokenPrincipal.getId()).isPresent()){ // и для него есть пользователь, то берем этого пользователя
                user = userRepository.findById(tokenPrincipal.getId()).get();
            } else { throw new BadRequestException(env.getProperty("error.TokenIsNotValid")); } // если для этого токена нет пользователя, то надо кидать ошибку. Это случай, когда в бд не правильно сохраняли)
        } else { throw new BadRequestException(env.getProperty("error.TokenIsNotValid")); } // если токен == null или у него id == null

        return user;
    }

}
