package com.psycorp.model.objects;

import com.psycorp.model.entity.AbstractEntity;
import com.psycorp.model.entity.User;
import com.psycorp.model.entity.UserAccountEntity;
import com.psycorp.model.entity.ValueCompatibilityAnswersEntity;
import com.psycorp.model.enums.AccountType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Object data level for collecting and transforming data from a database.
 * Collecting from {@link UserAccountEntity}, {@link User} {@link ValueCompatibilityAnswersEntity}
 * @author Maryna Kontar
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserAccount extends AbstractEntity {

    @NotNull @Valid
    private User user;
    private AccountType accountType;
    private Boolean isValueCompatibilityTestPassed;
    private List<String> inviteTokens;
    private List<User> usersForMatching;
    private List<User> usersWhoInvitedYou;
    private List<User> usersWhoYouInvite;
}
