package com.psycorp.service.implementation;

import com.psycorp.exception.BadRequestException;
import com.psycorp.model.entity.User;
import com.psycorp.model.entity.UserAccountEntity;
import com.psycorp.model.entity.ValueCompatibilityAnswersEntity;
import com.psycorp.model.enums.AccountType;
import com.psycorp.model.enums.TokenType;
import com.psycorp.model.objects.UserAccount;
import com.psycorp.model.security.TokenEntity;
import com.psycorp.repository.UserAccountRepository;
import com.psycorp.service.UserAccountService;
import com.psycorp.service.UserService;
import com.psycorp.service.ValueCompatibilityAnswersService;
import com.psycorp.service.security.TokenService;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserAccountServiceImpl implements UserAccountService {

    private final UserAccountRepository userAccountRepository;
    @Autowired
    private ValueCompatibilityAnswersService valueCompatibilityAnswersService;
//    @Autowired
    private final TokenService tokenService;
    private UserService userService;
    private final MongoOperations mongoOperations;

    @Autowired
    public UserAccountServiceImpl(UserAccountRepository userAccountRepository,
//                                  ValueCompatibilityAnswersService valueCompatibilityAnswersService,
//                                  TokenService tokenService,
//                                  UserService userService,
                                  TokenService tokenService, MongoOperations mongoOperations) {
        this.userAccountRepository = userAccountRepository;
        this.tokenService = tokenService;
//        this.valueCompatibilityAnswersService = valueCompatibilityAnswersService;
//        this.tokenService = tokenService;
//        this.userService = userService;
        this.mongoOperations = mongoOperations;
    }

    @Override
    public UserAccountEntity getUserAccountEntityByUserId(ObjectId userId) {
        return userAccountRepository.findByUserId(userId).orElseThrow(() ->
                new BadRequestException("user account not found for user id: " + userId));
    }

    @Override
    public UserAccount getUserAccount(User user) {
        UserAccountEntity userAccountEntity = getUserAccountEntityByUserIdOrNull(user.getId());
        if (userAccountEntity == null) { // if user aren`t registered and don`t have account
            return getAnonimUserAccount(user);
        }
        return getUserAccount(userAccountEntity);
    }

    private UserAccount getAnonimUserAccount(User user) {
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

    private UserAccount getUserAccount(UserAccountEntity userAccountEntity) {

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

    @Override
    public Page<UserAccount> getAllPageable(Pageable pageable) {
        Page<UserAccountEntity> page = userAccountRepository.findAll(pageable);
        Page<UserAccount> page1 = page.map(this::getUserAccount);
        return page1;
    }

    @Override
    public List<UserAccount> getAllRegisteredAndPassedTest() { // only thus who is registered (have userAccount) and pass test

        List<UserAccount> userAccounts = userAccountRepository.findAll().stream()
             .filter(userAccountEntity -> valueCompatibilityAnswersService.ifTestPassed(userAccountEntity.getUserId()))
             .filter(userAccountEntity -> !userAccountEntity.getUserId().equals(userService.getPrincipalUser().getId())) // not include principal user
             .map(this::getUserAccount)
        .collect(Collectors.toList());
        return userAccounts;
    }

    @Override
    public Page<UserAccount> getAllRegisteredAndPassedTestUserForMatchingPageable(Pageable pageable) { // only thus who is usersForMatching in user collection and passed test

        LookupOperation lookupOperation = LookupOperation.newLookup()
                .from("valueCompatibilityAnswersEntity")
                .localField("userId")
                .foreignField("userId")
                .as("userAccountEntityInfo");

        Fields fields = Fields.fields("_id", "userId", "accountType");
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("userId").in(userService.getPrincipalUser().getUsersForMatchingId())),
                lookupOperation,
                Aggregation.unwind("userAccountEntityInfo"),
                Aggregation.match(Criteria.where("userAccountEntityInfo").ne(null)),
                Aggregation.match(Criteria.where("userAccountEntityInfo.passed").is(true)),
                Aggregation.group(fields).addToSet("userId").as("userIds"),
                Aggregation.project(fields),
                Aggregation.sort(Sort.Direction.DESC, "userId"),
                //count, not use private List<UserAccountEntity>
                Aggregation.skip(((pageable.getPageNumber())*pageable.getPageSize())),
                Aggregation.limit(pageable.getPageSize())
        );

        AggregationResults<UserAccountEntity> aggregationResults = mongoOperations.aggregate(aggregation, "userAccountEntity", UserAccountEntity.class);
        List<UserAccountEntity> results = aggregationResults.getMappedResults();
        List<UserAccount> userAccounts = results.stream()
                .map(userAccountEntity -> getUserAccount(userAccountEntity))
                .collect(Collectors.toList());

        Page<UserAccount> page = new PageImpl<UserAccount>(userAccounts, pageable, getAllUserForMatchingPassedTest().size());
        return page;
    }

    @Override
    public List<UserAccount> getAllUserForMatchingPassedTest() {

        LookupOperation lookupOperation = LookupOperation.newLookup()
                .from("valueCompatibilityAnswersEntity")
                .localField("userId")
                .foreignField("userId")
                .as("userAccountEntityInfo");

        Fields fields = Fields.fields("_id", "userId", "accountType");
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("userId").in(userService.getPrincipalUser().getUsersForMatchingId())),
                lookupOperation,
                Aggregation.unwind("userAccountEntityInfo"),
                Aggregation.match(Criteria.where("userAccountEntityInfo").ne(null)),
                Aggregation.match(Criteria.where("userAccountEntityInfo.passed").is(true)),
                Aggregation.group(fields).addToSet("userId").as("userIds"),
                Aggregation.project(fields),
                Aggregation.sort(Sort.Direction.DESC, "userId")
        );

        AggregationResults<UserAccountEntity> aggregationResults = mongoOperations.aggregate(aggregation, "userAccountEntity", UserAccountEntity.class);
        List<UserAccountEntity> results = aggregationResults.getMappedResults();
        List<UserAccount> userAccounts = results.stream()
                .map(userAccountEntity -> getUserAccount(userAccountEntity))
                .collect(Collectors.toList());

        return userAccounts;
    }

    @Override
    public Page<UserAccount> getAllRegisteredAndPassedTestPageable(Pageable pageable) { // only thus who is registered (have userAccount) and passed test

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

        AggregationResults<UserAccountEntity> aggregationResults = mongoOperations.aggregate(aggregation, "userAccountEntity", UserAccountEntity.class);
        List<UserAccountEntity> results = aggregationResults.getMappedResults();
        List<UserAccount> userAccounts = results.stream()
                .map(userAccountEntity -> getUserAccount(userAccountEntity))
                .collect(Collectors.toList());

        Page<UserAccount> page = new PageImpl<UserAccount>(userAccounts, pageable, getAllRegisteredAndPassedTest().size());
        return page;
    }

    @Override
    public UserAccountEntity getUserAccountEntityByUserIdOrNull(ObjectId userId) {
        Optional<UserAccountEntity> userAccountEntityOptional = userAccountRepository.findByUserId(userId);
        UserAccountEntity userAccountEntity = userAccountEntityOptional.orElse(null);
        return userAccountEntity;
    }
//    public Map<AccountType, List<UserAccount>> getAllForMatching() {
//        List<User> users = userService.findAll();
//        List<UserAccount> userAccounts = new ArrayList<>();
//        users.forEach(user -> userAccounts.add(getUserAccount(user)));
//        Map<AccountType, List<UserAccount>> map = new HashMap<>();
//        userAccounts.
//        return userAccounts;
//    }

    @Override
    public UserAccountEntity insert(User user) {
        UserAccountEntity userAccountEntity = new UserAccountEntity();
        userAccountEntity.setUserId(user.getId());
        userAccountEntity.setAccountType(AccountType.OPEN);
       return userAccountRepository.insert(userAccountEntity);
    }

    @Override
    public List<User> getUsersForMatching() {
       return userService.getPrincipalUser()
               .getUsersForMatchingId().stream()
               .map(userService::findById)
               .collect(Collectors.toList());
    }

    @Override
    public UserAccount update(UserAccount userAccount) {
        userAccount.setUser(userService.updateUser(userAccount.getUser()));
        return userAccount;
    }

    @Override
    @Transactional
    public UserAccount inviteForMatching(UserAccount userAccount) {
        User principal = userService.getPrincipalUser();
        User user = userService.find(userAccount.getUser());
        UserAccountEntity userAccountEntityPrincipal = getUserAccountEntityByUserId(principal.getId());

        Query query = Query.query(Criteria.where(Fields.UNDERSCORE_ID).is(userAccountEntityPrincipal.getId()));
        Update update = new Update().push("usersWhoInvitedYou").atPosition(Update.Position.FIRST).value(user.getId());
        userAccountEntityPrincipal = mongoOperations.findAndModify(query, update,
                new FindAndModifyOptions().returnNew(true), UserAccountEntity.class);

        userAccount = getUserAccount(user);
        return userAccount;
    }
    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Data
    private class UserAccountInfo {
        private ObjectId id;
        private ObjectId userId;
        private AccountType accountType;
        private List<ObjectId> usersWhoInvitedYouId; // пользователи, которые пригласили тебя сравнить профили
        private List<ObjectId> usersWhoYouInviteId; // пользователи, которых ты пригласил сравнить профили
        private ValueCompatibilityAnswersEntity userAccountEntityInfo;
        private List<ObjectId> ids;
        private Integer count;
    }
}
