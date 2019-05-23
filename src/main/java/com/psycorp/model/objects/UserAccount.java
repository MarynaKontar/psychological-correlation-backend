package com.psycorp.model.objects;

import com.psycorp.model.entity.AbstractEntity;
import com.psycorp.model.entity.User;
import com.psycorp.model.enums.AccountType;
import lombok.Data;
import org.bson.types.ObjectId;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
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
