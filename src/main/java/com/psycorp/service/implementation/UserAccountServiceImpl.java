package com.psycorp.service.implementation;

import com.psycorp.exception.BadRequestException;
import com.psycorp.model.entity.User;
import com.psycorp.model.entity.UserAccountEntity;
import com.psycorp.model.enums.AccountType;
import com.psycorp.model.enums.TokenType;
import com.psycorp.model.objects.UserAccount;
import com.psycorp.model.security.TokenEntity;
import com.psycorp.repository.UserAccountRepository;
import com.psycorp.service.UserAccountService;
import com.psycorp.service.UserService;
import com.psycorp.service.ValueCompatibilityAnswersService;
import com.psycorp.service.security.TokenService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.psycorp.security.SecurityConstant.ACCESS_TOKEN_PREFIX;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * Service implementation for UserAccountService.
 * @author Maryna Kontar
 */
@Service
public class UserAccountServiceImpl implements UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final ValueCompatibilityAnswersService valueCompatibilityAnswersService;
    private final TokenService tokenService;
    private final UserService userService;
    private final MongoOperations mongoOperations;
    private final Environment env;

    @Autowired
    public UserAccountServiceImpl(UserAccountRepository userAccountRepository,
                                  ValueCompatibilityAnswersService valueCompatibilityAnswersService,
                                  TokenService tokenService,
                                  UserService userService,
                                  MongoOperations mongoOperations,
                                  Environment env) {
        this.userAccountRepository = userAccountRepository;
        this.valueCompatibilityAnswersService = valueCompatibilityAnswersService;
        this.tokenService = tokenService;
        this.userService = userService;
        this.mongoOperations = mongoOperations;
        this.env = env;
    }

    /**
     * Insert new {@link UserAccountEntity} for saved in database user.
     * @param user must not be {@literal null}, must be saved in database.
     * @return inserted {@link UserAccountEntity}.
     */
    @Override
    public UserAccountEntity insert(User user) {
        if (user == null) {
            throw new BadRequestException(env.getProperty("error.UserCan`tBeNull"));
        }
        UserAccountEntity userAccountEntity = new UserAccountEntity();
        userAccountEntity.setUserId(user.getId());
        userAccountEntity.setAccountType(AccountType.OPEN);
        return userAccountRepository.insert(userAccountEntity);
    }

    /**
     * Gives an user account for user, if there is, or create anonymous account without writing to the database.
     * For the anonymous user does not check whether there is a user in the database.
     * @param user for whom the account is being getting
     * @return user account obtained by conversion UserAccountEntity from the database, if there is,
     * or created anonymous account without writing to the database
     */
    @Override
    public UserAccount getUserAccount(User user) {
        UserAccountEntity userAccountEntity = getUserAccountEntityByUserIdOrNull(user.getId());
        if (userAccountEntity == null) { // if user aren`t registered and don`t have account
            return convertToAnonimUserAccount(user);
        }
        return convertToUserAccount(userAccountEntity);
    }

    /**
     * Gets list of {@link UserAccount} for all registered users from userForMatching of principal user
     * that passed value compatibility test.
     * @return list of {@link UserAccount}.
     */
    @Override
    public List<UserAccount> getAllUserForMatchingPassedTest() {

        LookupOperation lookupOperation = LookupOperation.newLookup()
                .from("valueCompatibilityAnswersEntity") // lookup from valueCompatibilityAnswersEntity collection
                .localField("userId") // field from userAccountEntity collection
                .foreignField("userId") // field from valueCompatibilityAnswersEntity collection
                .as("userAccountEntityInfo"); // the obtained result of lookup operation will be saved as userAccountEntityInfo

        Fields fields = Fields.fields("_id", "userId", "accountType");
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("userId")
                        .in(userService.getPrincipalUser().getUsersForMatchingId())),
                lookupOperation,
                unwind("userAccountEntityInfo"), // unwind results for user with multiply valueCompatibilityAnswersEntities
                Aggregation.match(Criteria.where("userAccountEntityInfo").ne(null)),
                Aggregation.match(Criteria.where("userAccountEntityInfo.passed").is(true)),
                group(fields).addToSet("userId").as("userIds"), // only unique
                project(fields), // remains only fields
                Aggregation.sort(Sort.Direction.DESC, "userId")
        );

        AggregationResults<UserAccountEntity> aggregationResults = mongoOperations
                .aggregate(aggregation, "userAccountEntity", UserAccountEntity.class);
        List<UserAccountEntity> results = aggregationResults.getMappedResults();
        List<UserAccount> userAccounts = results.stream()
                .map(userAccountEntity -> convertToUserAccount(userAccountEntity))
                .collect(Collectors.toList());

        //add users that pass test but not registered yet
        return userAccounts;
    }

    /**
     * Gets a {@link Page} of {@link UserAccount} for registered users,
     * that passed value compatibility test (except principal user)
     * meeting the paging restriction provided in the {@code Pageable} object.
     * @param pageable {@code Pageable} object that defines page options, must not be {@literal null}.
     * @return a page of {@link UserAccount} for registered users,
     * that passed value compatibility test (except principal user).
     */
    @Override
    public Page<UserAccount> getAllRegisteredAndPassedTestPageable(Pageable pageable) {

        LookupOperation lookupOperation = LookupOperation.newLookup()
                .from("valueCompatibilityAnswersEntity")
                .localField("userId")
                .foreignField("userId")
                .as("userAccountEntityInfo");

        Fields fields = Fields.fields("_id", "userId", "accountType");
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("userId").ne(userService.getPrincipalUser().getId())),
                lookupOperation,
                Aggregation.unwind("userAccountEntityInfo"),
                Aggregation.match(Criteria.where("userAccountEntityInfo").ne(null)),
                Aggregation.match(Criteria.where("userAccountEntityInfo.passed").is(true)),
                Aggregation.group(fields).addToSet("userId").as("userIds"),
                Aggregation.project(fields),
                Aggregation.sort(Sort.Direction.DESC, "userId"),

                Aggregation.skip(((pageable.getPageNumber())*pageable.getPageSize())),
                Aggregation.limit(pageable.getPageSize())
        );

        AggregationResults<UserAccountEntity> aggregationResults = mongoOperations
                .aggregate(aggregation, "userAccountEntity", UserAccountEntity.class);
        List<UserAccountEntity> results = aggregationResults.getMappedResults();
        List<UserAccount> userAccounts = results.stream()
                .map(userAccountEntity -> convertToUserAccount(userAccountEntity))
                .collect(Collectors.toList());
        Page<UserAccount> page = new PageImpl<>(userAccounts, pageable, getAllRegisteredAndPassedTest().size());
        return page;
    }

    /**
     * Gets {@link UserAccountEntity} by userId user id.
     * @param userId must not be {@literal null}.
     * @return {@link UserAccountEntity} or {@literal null} if none exists.
     */
    @Override
    public UserAccountEntity getUserAccountEntityByUserIdOrNull(ObjectId userId) {
//        Optional<UserAccountEntity> userAccountEntityOptional = userAccountRepository.findByUserId(userId);
//        UserAccountEntity userAccountEntity = userAccountEntityOptional.orElse(null);
        return userAccountRepository.findByUserId(userId).orElse(null);
    }

    /**
     * Returns singleton list with user retrieves by userForMatchingToken
     * or list of usersForMatching for principal user if userForMatchingToken is {@literal null}.
     * @param userForMatchingToken
     * @return singleton list with user retrieves by userForMatchingToken or list of usersForMatching
     * for principal user if userForMatchingToken is {@literal null}.
     */
    @Override
    public List<User> getUsersForMatching(String userForMatchingToken) {
        if (userForMatchingToken != null) {
            userForMatchingToken = userForMatchingToken.substring(ACCESS_TOKEN_PREFIX.length() + 1);
            User user = tokenService.getUserByToken(userForMatchingToken);
            return Collections.singletonList(user);
        }
       return userService.getPrincipalUser()
               .getUsersForMatchingId().stream()
               .map(userService::findById)
               .collect(Collectors.toList());
    }

    /**
     * Updates {@link UserAccount}.
     * @param userAccount must not be {@literal null}.
     * @return updated user account.
     */
    @Override
    public UserAccount update(UserAccount userAccount) {
        userAccount.setUser(userService.updateUser(userAccount.getUser()));
        return userAccount;
    }

    /**
     * Adds principal user id to usersWhoInvitedYouId field of {@link UserAccountEntity} for userAccount
     * and id of user from userAccount to usersWhoYouInviteId field of {@link UserAccountEntity} for principal user.
     * @param userAccount must not be {@literal null}.
     * @return updated user account.
     */
    @Override
    @Transactional
    public UserAccount inviteForMatching(UserAccount userAccount) {
        User principal = userService.getPrincipalUser();
        User user = userService.find(userAccount.getUser());
        UserAccountEntity userAccountEntityPrincipal = getUserAccountEntityByUserId(principal.getId());

        // UPDATE PRINCIPAL USER
        Query query = Query.query(Criteria.where(Fields.UNDERSCORE_ID).is(userAccountEntityPrincipal.getId()));
        Update update = new Update().push("usersWhoYouInviteId").atPosition(Update.Position.FIRST).value(user.getId());
        userAccountEntityPrincipal = mongoOperations.findAndModify(query, update,
                new FindAndModifyOptions().returnNew(true), UserAccountEntity.class);

        // UPDATE INVITEE USER
        Query query1 = Query.query(Criteria.where(Fields.UNDERSCORE_ID).is(user.getId()));
        Update update1 = new Update().push("usersWhoInvitedYouId").atPosition(Update.Position.FIRST)
                .value(userAccountEntityPrincipal.getId());
        UserAccountEntity userAccountEntity = mongoOperations.findAndModify(query1, update1,
                new FindAndModifyOptions().returnNew(true), UserAccountEntity.class);

        userAccount = convertToUserAccount(userAccountEntity);
        return userAccount;
    }

    /**
     * Creates {@link UserAccount} for user without creating and writing {@link UserAccountEntity} to the database.
     * @param user must not be {@literal null}.
     * @return {@link UserAccount} for user.
     */
    private UserAccount convertToAnonimUserAccount(User user) {
        UserAccount userAccount = new UserAccount();
        Boolean isValueCompatibilityTestPassed = valueCompatibilityAnswersService.ifTestPassed(user.getId());

        List<String> inviteTokens = new ArrayList<>();
        List<User> usersForMatching = new ArrayList<>();
        List<ObjectId> usersForMatchingId = user.getUsersForMatchingId();

        if (usersForMatchingId != null && !usersForMatchingId.isEmpty()) {
            inviteTokens = usersForMatchingId.stream() // only those who has INVITE_TOKEN
                    .map(userForMatchingId ->
                            tokenService.findByUserIdAndTokenType(userForMatchingId, TokenType.INVITE_TOKEN))
                    .filter(Objects::nonNull)
                    .map(TokenEntity::getToken)
                    .collect(Collectors.toList());
            usersForMatching = usersForMatchingId.stream() // only those who passed the test
                    .map(userService::findById)
                    .filter(userForMatching -> valueCompatibilityAnswersService.ifTestPassed(userForMatching.getId()))
                    .collect(Collectors.toList());
        }

        userAccount.setUser(user);
        userAccount.setIsValueCompatibilityTestPassed(isValueCompatibilityTestPassed);
        userAccount.setAccountType(AccountType.OPEN);
        userAccount.setInviteTokens(inviteTokens);
        userAccount.setUsersForMatching(usersForMatching);
        return userAccount;
    }

    /**
     * Gets all {@link UserAccount} for registered and passed value compatibility test users.
     * @return list of {@link UserAccount}.
     */
    private List<UserAccount> getAllRegisteredAndPassedTest() { // only thus who is registered (have userAccount) and pass test

        List<UserAccount> userAccounts = userAccountRepository.findAll().stream()
                .filter(userAccountEntity -> valueCompatibilityAnswersService.ifTestPassed(userAccountEntity.getUserId()))
                .filter(userAccountEntity -> !userAccountEntity.getUserId().equals(userService.getPrincipalUser().getId())) // not include principal user
                .map(this::convertToUserAccount)
                .collect(Collectors.toList());
        return userAccounts;
    }

    /**
     * Convert {@link UserAccountEntity} to {@link UserAccount}.
     * @param userAccountEntity must not be {@literal null}.
     * @return {@link UserAccount}.
     */
    private UserAccount convertToUserAccount(UserAccountEntity userAccountEntity) {

        UserAccount userAccount = new UserAccount();
        User user = userService.findById(userAccountEntity.getUserId());
        Boolean isValueCompatibilityTestPassed = valueCompatibilityAnswersService.ifTestPassed(userAccountEntity.getUserId());

        List<String> inviteTokens = new ArrayList<>();
        List<User> usersForMatching = new ArrayList<>();
        List<User> usersWhoInvitedYou = new ArrayList<>();
        List<User> usersWhoYouInvite = new ArrayList<>();
        List<ObjectId> usersForMatchingId = user.getUsersForMatchingId();
        List<ObjectId> usersWhoInvitedYouId = userAccountEntity.getUsersWhoInvitedYouId();
        List<ObjectId> usersWhoYouInviteId = userAccountEntity.getUsersWhoYouInviteId();
        if (usersForMatchingId != null && !usersForMatchingId.isEmpty()) {
            inviteTokens = usersForMatchingId.stream() // only those who has INVITE_TOKEN
                    .map(userForMatchingId ->
                            tokenService.findByUserIdAndTokenType(userForMatchingId, TokenType.INVITE_TOKEN))
                    .filter(Objects::nonNull)
                    .map(TokenEntity::getToken)
                    .collect(Collectors.toList());
            usersForMatching = usersForMatchingId.stream() // only those who passed the test
                    .map(userService::findById)
                    .filter(userForMatching -> valueCompatibilityAnswersService.ifTestPassed(userForMatching.getId()))
                    .collect(Collectors.toList());
        }
        if(usersWhoInvitedYouId != null && !usersWhoInvitedYouId.isEmpty()) {
            usersWhoInvitedYou = usersWhoInvitedYouId.stream()
                    .map(userService::findById)
                    .collect(Collectors.toList());
        }
        if(usersWhoYouInviteId != null && !usersWhoYouInviteId.isEmpty()) {
            usersWhoYouInvite = usersWhoYouInviteId.stream()
                    .map(userService::findById)
                    .collect(Collectors.toList());
        }
        userAccount.setUser(user);
        userAccount.setIsValueCompatibilityTestPassed(isValueCompatibilityTestPassed);
        userAccount.setAccountType(userAccountEntity.getAccountType());
        userAccount.setInviteTokens(inviteTokens);
        userAccount.setUsersForMatching(usersForMatching);
        userAccount.setUsersWhoInvitedYou(usersWhoInvitedYou);
        userAccount.setUsersWhoYouInvite(usersWhoYouInvite);
        return userAccount;
    }

    /**
     * Gets {@link UserAccountEntity} by userId.
     * @param userId  must not be {@literal null}.
     * @return {@link UserAccountEntity}.
     */
    private UserAccountEntity getUserAccountEntityByUserId(ObjectId userId) {
        return userAccountRepository.findByUserId(userId).orElseThrow(() ->
                new BadRequestException("user account not found for user id: " + userId));
    }

}
