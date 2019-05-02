package com.psycorp.service.implementation;

import com.psycorp.model.entity.User;
import com.psycorp.model.enums.TokenType;
import com.psycorp.model.objects.UserAccount;
import com.psycorp.model.security.TokenEntity;
import com.psycorp.service.UserAccountService;
import com.psycorp.service.UserService;
import com.psycorp.service.ValueCompatibilityAnswersService;
import com.psycorp.service.security.TokenService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class UserAccountServiceImpl implements UserAccountService {

    private final ValueCompatibilityAnswersService valueCompatibilityAnswersService;
    private final TokenService tokenService;
    private final UserService userService;

    public UserAccountServiceImpl(ValueCompatibilityAnswersService valueCompatibilityAnswersService, TokenService tokenService, UserService userService) {
        this.valueCompatibilityAnswersService = valueCompatibilityAnswersService;
        this.tokenService = tokenService;
        this.userService = userService;
    }

    @Override
    public UserAccount getUserAccount(User user) {
        UserAccount userAccount = new UserAccount();
        Boolean isValueCompatibilityTestPassed = valueCompatibilityAnswersService.ifTestPassed(user);

        List<String> inviteTokens = new ArrayList<>();
        AtomicReference<TokenEntity> tokenEntity = new AtomicReference<>();
        List<User> usersForMatching = user.getUsersForMatching();
        if (usersForMatching != null && !usersForMatching.isEmpty()) {
//            usersForMatching.forEach(userForMatching -> {
//                tokenEntity.set(tokenService.findByUserIdAndTokenType(userForMatching.getId(), TokenType.INVITE_TOKEN));
//                if (tokenEntity.get() != null) {
//                    inviteTokens.add(tokenEntity.get().getToken());
//                }
//            });
            inviteTokens = usersForMatching.stream()
                    .map(userForMatching ->
                            tokenService.findByUserIdAndTokenType(userForMatching.getId(), TokenType.INVITE_TOKEN))
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
}
