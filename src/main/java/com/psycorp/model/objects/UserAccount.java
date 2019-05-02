package com.psycorp.model.objects;

import com.psycorp.model.entity.AbstractEntity;
import com.psycorp.model.entity.User;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class UserAccount extends AbstractEntity {
    @NotNull @Valid
    private User user;

    private Boolean isValueCompatibilityTestPassed;
    private List<String> inviteTokens;

}
