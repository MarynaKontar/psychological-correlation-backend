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
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

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
    public List<UserAccount> getAllRegisteredAndPassedTest() { // only thus who is registered (have userAccount) and pass test

        List<UserAccount> userAccounts = userAccountRepository.findAll().stream()
             .filter(userAccountEntity -> valueCompatibilityAnswersService.ifTestPassed(userAccountEntity.getUserId()))
             .map(this::getUserAccount)
        .collect(Collectors.toList());

        // what will be faster? many queries to valueCompatibilityAnswersEntity collection or to user collection
//        List<UserAccount> userAccounts = userAccountRepository.findAll().stream()
//                .map(this::getUserAccount)
//                .filter(UserAccount::getIsValueCompatibilityTestPassed)
//                .collect(Collectors.toList());
        return userAccounts;
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
        return getUserAccountEntityByUserId(userService.getPrincipalUser().getId())
                .getUsersWhoYouInviteId()
                .stream()
                .map(userId -> userService.findById(userId))
                .collect(Collectors.toList());
//        UserAccount userAccount = userAccountService.getUserAccount(getPrincipalUser());
//        return userAccount.getUsersForMatching();
    }

    @Override
    public UserAccount update(UserAccount userAccount) {
        userAccount.setUser(userService.updateUser(userAccount.getUser()));
        return userAccount;
    }

    @Override
    public UserAccount inviteForMatching(UserAccount userAccount) {
        User principal = userService.getPrincipalUser();
        User user = userService.find(userAccount.getUser());
        UserAccountEntity userAccountEntityPrincipal = getUserAccountEntityByUserId(principal.getId());

        Query query = Query.query(Criteria.where(Fields.UNDERSCORE_ID).is(userAccountEntityPrincipal.getId()));
        Update update = new Update().push("usersWhoInvitedYou").atPosition(Update.Position.FIRST).value(user.getId());
        userAccountEntityPrincipal = mongoOperations.findAndModify(query, update,
                new FindAndModifyOptions().returnNew(true), UserAccountEntity.class);
//        mongoOperations.updateFirst(query, update, UserAccountEntity.class);

        //TODO написать метод для UserAccountEntity -> UserAccount
        userAccount = getUserAccount(user);
        return userAccount;
    }
    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
