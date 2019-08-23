package com.psycorp.service.implementation;

import com.psycorp.exception.AuthorizationException;
import com.psycorp.exception.BadRequestException;
import com.psycorp.model.entity.User;
import com.psycorp.model.enums.ErrorEnum;
import com.psycorp.model.enums.UserRole;
import com.psycorp.model.security.CredentialsEntity;
import com.psycorp.repository.ValueCompatibilityAnswersRepository;
import com.psycorp.repository.UserMatchRepository;
import com.psycorp.repository.UserRepository;
import com.psycorp.repository.security.CredentialsRepository;
import com.psycorp.repository.security.TokenRepository;
import com.psycorp.security.token.TokenPrincipal;
import com.psycorp.service.security.AuthService;
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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for UserService.
 * @author Maryna Kontar
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CredentialsRepository credentialsRepository;
    private final ValueCompatibilityAnswersRepository valueCompatibilityAnswersRepository;
    private final UserMatchRepository userMatchRepository;
    private final AuthService authService;
    private final TokenRepository tokenRepository;
    private final MongoOperations mongoOperations;
    private final Environment env;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           CredentialsRepository credentialsRepository,
                           ValueCompatibilityAnswersRepository valueCompatibilityAnswersRepository,
                           UserMatchRepository userMatchRepository,
                           AuthService authService,
                           TokenRepository tokenRepository,
                           MongoOperations mongoOperations,
                           Environment env) {
        this.userRepository = userRepository;
        this.credentialsRepository = credentialsRepository;
        this.valueCompatibilityAnswersRepository = valueCompatibilityAnswersRepository;
        this.userMatchRepository = userMatchRepository;
        this.authService = authService;
        this.tokenRepository = tokenRepository;
        this.mongoOperations = mongoOperations;
        this.env = env;
    }

    /**
     * Creates anonim user with role ANONIM, without password.
     * @return created user.
     */
    @Override
    @Transactional
    public User createAnonimUser() {
        User user = new User();
        user.setName(UUID.randomUUID().toString());
        user.setRole(UserRole.ANONIM);
        user = userRepository.save(user);
        CredentialsEntity credentialsEntity = new CredentialsEntity();
        credentialsEntity.setUserId(user.getId());
        credentialsRepository.save(credentialsEntity);
        return user;
    }

    /**
     * Saves name, age and gender to principal user.
     * If in user will be an email, it will not be saved. User role isn't changed.
     * @param user that contain incomplete user information (name, age and gender), must not be {@literal null}.
     * @return updated user
     * @throws BadRequestException if user or user name, age or gender are {@literal null}
     */
    @Override
    public User addNameAgeAndGender(User user) {
        if(user == null || user.getName() == null || user.getAge() == null || user.getGender() == null) {
            throw new BadRequestException("User name, age or gender cant be null");
        }
        User principal = getPrincipalUser();

        // CHECK NAME ON UNIQUE
        if (!principal.getName().equals(user.getName())) {
            checkIfUsernameOrEmailExist(user.getName());
        }

        // UPDATE USER
        Update updateUser = new Update()
                .set("name", (user.getName() != null && !user.getName().isEmpty()) ? user.getName() : principal.getName())
                .set("age", (user.getAge() != null) ? user.getAge() : principal.getAge())
                .set("gender", (user.getGender() != null) ? user.getGender() : principal.getGender());
        Query queryUser = Query.query(Criteria.where(Fields.UNDERSCORE_ID).is(principal.getId()));
        user = mongoOperations.findAndModify(queryUser, updateUser
                , new FindAndModifyOptions().returnNew(true), User.class);
        return user;
    }

    /**
     * Adds ids of all users from usersForMatching to field usersForMatchingId of user at position.
     * @param user user to whom we are adding ids to field usersForMatchingId at position, must not be {@literal null}.
     * @param usersForMatching users whose ids we are adding to user, must not be {@literal null}.
     * @param position the position to which we will add ids, must not be {@literal null}.
     * @return updated user.
     */
    @Override
    public User addNewUsersForMatching(User user, List<User> usersForMatching, Update.Position position){
        Update updateUser = new Update();
        Set<ObjectId> usersForMatchingId = usersForMatching.stream().map(User::getId).collect(Collectors.toSet());
        updateUser.push("usersForMatchingId")
                .atPosition(position)
                .each(usersForMatchingId);
        Query queryUser = Query.query(Criteria.where(Fields.UNDERSCORE_ID).is(user.getId()));
        user = mongoOperations.findAndModify(queryUser, updateUser,
                new FindAndModifyOptions().returnNew(true), User.class);
        return user;
    }

    /**
     * Finds user.
     * @param user the user by whose id the user is taken from the database.
     * @return user from database, must not be {@literal null}.
     * @throws BadRequestException if user or user id is {@literal null}.
     */
    @Override
    public User find(User user) {
        if (user != null && user.getId() != null) {
            user = findById(user.getId());
        } else {
            throw new BadRequestException(env.getProperty("error.UserOrUserIdCan`tBeNull"));
        }
        return user;
    }

    /**
     * Finds user by userId.
     * @param userId the id of the user, must not be {@literal null}..
     * @return user from database.
     * @throws IllegalArgumentException if {@code id} is {@literal null}.
     * @throws BadRequestException if none found.
     */
    @Override
    public User findById(ObjectId userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException(env.getProperty("error.noUserFound") + " for user id: " + userId));
    }

    /**
     * Finds user by name or email {@code nameOrEmail}.
     * @param nameOrEmail name or email of user, must not be {@literal null}.
     * @return user from database.
     * @throws BadRequestException if nameOrEmail is {@literal null} or none found.
     */
    @Override
    public User findUserByNameOrEmail(String nameOrEmail) {
        if(nameOrEmail == null) throw new BadRequestException(env.getProperty("error.noUserFound"));
        return userRepository.findUserByNameOrEmail(nameOrEmail, nameOrEmail)
                .orElseThrow(() -> new BadRequestException(env.getProperty("error.noUserFound") + " for name or email: "
                        + nameOrEmail));
    }

    /**
     * Gets principal user.
     * @return principal user.
     * @throws AuthorizationException if none found.
     */
    @Override
    public User getPrincipalUser() {
        TokenPrincipal tokenPrincipal = (TokenPrincipal) authService.getAuthPrincipal();
        if(tokenPrincipal != null && tokenPrincipal.getId() != null) { //if token exist
            return findById(tokenPrincipal.getId()); // and for it there is user, than take this user
        } else { throw new AuthorizationException("User not authorised", ErrorEnum.NOT_AUTHORIZED); } // if token == null or token id == null
    }

    /**
     * Checks whether an user with given name or email exists.
     * @param nameOrEmail must not be {@literal null}.
     * @throws BadRequestException if user with given name or email {@code nameOrEmail} exists
     * or if {@code nameOrEmail} is {@literal null}.
     */
    @Override
    public void checkIfUsernameOrEmailExist(String nameOrEmail) {
        if(nameOrEmail == null) throw new BadRequestException(env.getProperty("error.UserNameOrEmailCan`tBeNull"));

        if(userRepository.findUserByNameOrEmail(nameOrEmail, nameOrEmail).isPresent()) {
            throw new BadRequestException(env.getProperty("error.UserWithTheseNameOrEmailAlreadyExists") + ": "
                    + nameOrEmail);
        }
    }

    /**
     * Returns whether an user with the given id exists.
     * @param userId must not be {@literal null}.
     * @return {@literal true} if an user with the given id exists, {@literal false} otherwise.
     * @throws IllegalArgumentException if {@code id} is {@literal null}.
     */
    @Override
    public boolean checkIfExistById(ObjectId userId) {
        return userRepository.existsById(userId);
    }

    /**
     * Updates fields role, name, age, gender and email for principal user.
     * Field role becomes USER.
     * @param user with information for updating, must not be {@literal null}.
     * @return updated principal user.
     * @throws BadRequestException if user is {@literal null}.
     */
    @Override
    public User updateUser(User user) {
        if (user == null) {throw new BadRequestException(env.getProperty("error.UserCan`tBeNull"));}
        User principal = getPrincipalUser();
        Update updateUser = new Update();
        if (user.getRole() != null) { updateUser.set("role", UserRole.USER); }
        if(user.getName() != null) { updateUser.set("name", user.getName()); }
        if(user.getAge() != null) { updateUser.set("age", user.getAge()); }
        if(user.getGender() != null) { updateUser.set("gender", user.getGender()); }
        if(user.getEmail() != null) { updateUser.set("email", user.getEmail()); }

        Query queryUser = Query.query(Criteria.where(Fields.UNDERSCORE_ID).is(principal.getId()));
        user = mongoOperations.findAndModify(queryUser, updateUser,
                new FindAndModifyOptions().returnNew(true), User.class);// returns already updated document (returnNew(true))
        return user;
    }


    /**
     * Deletes user from database. In addition to the user himself, deletes his account, tests,
     * and deletes his ID from other users.
     * TODO method isn't finished!!! Don't use it!!!
     * @param userId id of the user to be deleted.
     * @return deleted user.
     */
    @Override
    @Transactional
    public User deleteUser(ObjectId userId) {
        // check on principal
        // authUtil.userAuthorization(userId);
        //TODO remove user from usersForMatchingId in all users
        User user = findById(userId);
        valueCompatibilityAnswersRepository.removeAllByUserId(userId);
        userMatchRepository.removeAllByUsersId(userId);
        tokenRepository.removeAllByUserId(userId);
        //remove user from usersForMatchingId of another users
        userRepository.delete(user);
        return user;
    }
}
