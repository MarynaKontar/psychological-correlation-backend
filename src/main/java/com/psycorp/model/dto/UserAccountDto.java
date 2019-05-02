package com.psycorp.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class UserAccountDto extends AbstractDto{

    @NotNull
    private SimpleUserDto user;
    private Boolean isValueCompatibilityTestPassed;
    private List<String> inviteTokens;
}
