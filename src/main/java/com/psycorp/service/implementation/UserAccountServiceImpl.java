package com.psycorp.service.implementation;

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
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class UserAccountServiceImpl implements UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final ValueCompatibilityAnswersService valueCompatibilityAnswersService;
    private final TokenService tokenService;
    private final UserService userService;
    private final MongoOperations mongoOperations;

    @Autowired
    public UserAccountServiceImpl(UserAccountRepository userAccountRepository,
                                  ValueCompatibilityAnswersService valueCompatibilityAnswersService,
                                  TokenService tokenService, UserService userService, MongoOperations mongoOperations) {
        this.userAccountRepository = userAccountRepository;
        this.valueCompatibilityAnswersService = valueCompatibilityAnswersService;
        this.tokenService = tokenService;
        this.userService = userService;
        this.mongoOperations = mongoOperations;
    }

    @Override
    public List<UserAccount> getAll() {
        List<User> users = userService.findAll();
        List<UserAccount> userAccounts = new ArrayList<>();
        users.forEach(user -> userAccounts.add(getUserAccount(user)));
        return userAccounts;
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
       UserAccountEntity userAccountEntity = getUserAccountEntity(user);
       userAccountEntity.setAccountType(AccountType.OPEN);
       return userAccountRepository.insert(userAccountEntity);
    }

    private UserAccountEntity getUserAccountEntity(User user) {
        UserAccountEntity userAccountEntity = new UserAccountEntity();
        userAccountEntity.setUserId(user.getId());
        userAccountEntity.setAccountType(AccountType.OPEN);
        return userAccountEntity;
    }

    @Override
    public UserAccount getUserAccount(User user) {
        UserAccount userAccount = new UserAccount();
        Boolean isValueCompatibilityTestPassed = valueCompatibilityAnswersService.ifTestPassed(user);

        List<String> inviteTokens = new ArrayList<>();
        AtomicReference<TokenEntity> tokenEntity = new AtomicReference<>();
        List<ObjectId> usersForMatchingId = user.getUsersForMatchingId();
        if (usersForMatchingId != null && !usersForMatchingId.isEmpty()) {
//            usersForMatchingId.forEach(userForMatching -> {
//                tokenEntity.set(tokenService.findByUserIdAndType(userForMatching.getId(), TokenType.INVITE_TOKEN));
//                if (tokenEntity.get() != null) {
//                    inviteTokens.add(tokenEntity.get().getToken());
//                }
//            });
            inviteTokens = usersForMatchingId.stream()
                    .map(userForMatching ->
                            tokenService.findByUserIdAndTokenType(userForMatching, TokenType.INVITE_TOKEN))
                    .filter(Objects::nonNull)
                    .map(TokenEntity::getToken)
                    .collect(Collectors.toList());
        }

        userAccount.setUser(user);
        userAccount.setIsValueCompatibilityTestPassed(isValueCompatibilityTestPassed);
        userAccount.setInviteTokens(inviteTokens);
        return userAccount;
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
        UserAccountEntity userAccountEntityPrincipal = userAccountRepository.findFirstByUserId(principal.getId());

        Query query = Query.query(Criteria.where(Fields.UNDERSCORE_ID).is(userAccountEntityPrincipal.getId()));
        Update update = new Update().push("usersWhoInvitedYou").atPosition(Update.Position.FIRST).value(user);
        userAccountEntityPrincipal = mongoOperations.findAndModify(query, update,
                new FindAndModifyOptions().returnNew(true), UserAccountEntity.class);
//        mongoOperations.updateFirst(query, update, UserAccountEntity.class);

        //TODO написать метод для UserAccountEntity -> UserAccount
        userAccount = getUserAccount(user);
        return userAccount;
    }
}
