package com.psycorp.model.dto;

import com.psycorp.model.enums.AccountType;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class UserAccountDto extends AbstractDto{

    @NotNull @Valid
    private SimpleUserDto user;
    private AccountType accountType;
    private Boolean isValueCompatibilityTestPassed;
    private List<String> inviteTokens;
    private List<SimpleUserDto> usersForMatching;
    private List<SimpleUserDto> usersWhoYouInvite;
    private List<SimpleUserDto> usersWhoInvitedYou;
}
