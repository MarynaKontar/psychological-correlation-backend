package com.psycorp.service.implementation;

import com.psycorp.exception.AuthorizationException;
import com.psycorp.exception.BadRequestException;
import com.psycorp.model.dto.SomeDto;
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
import com.psycorp.service.UserAccountService;
import com.psycorp.service.security.AuthService;
import com.psycorp.util.AuthUtil;
import com.psycorp.service.UserService;
import org.bson.types.ObjectId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CredentialsRepository credentialsRepository;
//    @Autowired
//    private final UserAccountService userAccountService;
    private final ValueCompatibilityAnswersRepository valueCompatibilityAnswersRepository;
    private final UserMatchRepository userMatchRepository;
//    @Autowired
    private final AuthService authService;
    private final TokenRepository tokenRepository;
    private final MongoOperations mongoOperations;
//    private final AuthUtil authUtil;
    private final Environment env;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           CredentialsRepository credentialsRepository,
//                           UserAccountService userAccountService,
                           ValueCompatibilityAnswersRepository valueCompatibilityAnswersRepository,
                           UserMatchRepository userMatchRepository,
                           AuthService authService, TokenRepository tokenRepository,
                           MongoOperations mongoOperations,
//                           AuthUtil serviceUtil,
                           Environment env) {
        this.userRepository = userRepository;
        this.credentialsRepository = credentialsRepository;
//        this.userAccountService = userAccountService;
        this.valueCompatibilityAnswersRepository = valueCompatibilityAnswersRepository;
        this.userMatchRepository = userMatchRepository;
        this.authService = authService;
        this.tokenRepository = tokenRepository;
        this.mongoOperations = mongoOperations;
//        this.authUtil = serviceUtil;
        this.env = env;
    }

    @Override
    @Transactional
    public User createAnonimUser() {
        User user = new User();
        user.setName(UUID.randomUUID().toString());
        user.setRole(UserRole.ANONIM);
        user = userRepository.save(user);
//        user = userRepository.save(new User(UUID.randomUUID().toString(), UserRole.ANONIM));
        CredentialsEntity credentialsEntity = new CredentialsEntity();
        credentialsEntity.setUserId(user.getId());
        credentialsRepository.save(credentialsEntity);
        return user;
    }

    @Override
    public User addNameAgeAndGender(User user) {
        User principal = getPrincipalUser();
        Update updateUser = new Update()
                .set("name", (user.getName() != null && !user.getName().isEmpty()) ? user.getName() : principal.getName())
                .set("age", (user.getAge() != null) ? user.getAge() : principal.getAge())
                .set("gender", (user.getGender() != null) ? user.getGender() : principal.getGender());

        Query queryUser = Query.query(Criteria.where(Fields.UNDERSCORE_ID).is(principal.getId()));
        user = mongoOperations.findAndModify(queryUser, updateUser
                , new FindAndModifyOptions().returnNew(true), User.class);
        return user;
    }

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
        if(nameOrEmail == null) throw new BadRequestException(env.getProperty("error.noUserFound"));
        return userRepository.findUserByNameOrEmail(nameOrEmail, nameOrEmail)
                .orElseThrow(() -> new BadRequestException(env.getProperty("error.noUserFound") + " for name or email: "
                        + nameOrEmail));
    }

    //TODO delete
    @Override
    public User findFirstUserByName(String name) {
        if(name == null) throw new BadRequestException(env.getProperty("error.noUserFound"));
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
    public List<SomeDto> getVCAnswersWithUserInfo() {
        LookupOperation lookupOperation = LookupOperation.newLookup()
                .from("user")
                .localField("user.$id")
                .foreignField("id")
                .as("userInfo");

        Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(Criteria.where("passed").is(true)), lookupOperation);

//        List<SomeDto> results = mongoOperations.aggregate(aggregation, "valueCompatibilityAnswersEntity", SomeDto.class).getMappedResults();
        AggregationResults<SomeDto> aggregationResults = mongoOperations.aggregate(aggregation, "valueCompatibilityAnswersEntity", SomeDto.class);
        List<SomeDto> results = aggregationResults.getMappedResults();
        return results;
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
        user = mongoOperations.findAndModify(queryUser, updateUser,
                new FindAndModifyOptions().returnNew(true), User.class);// вернет уже измененный документ (returnNew(true))
        return user;
    }


    @Override
    @Transactional
    public User deleteUser(ObjectId userId) {
//проверку на principal
//        authUtil.userAuthorization(userId);
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
