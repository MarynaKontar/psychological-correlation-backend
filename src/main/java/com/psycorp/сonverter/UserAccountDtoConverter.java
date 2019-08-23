package com.psycorp.сonverter;

import com.psycorp.model.dto.SimpleUserDto;
import com.psycorp.model.dto.UserAccountDto;
import com.psycorp.model.entity.User;
import com.psycorp.model.objects.UserAccount;
import org.springframework.stereotype.Component;

/**
 * Dto converter for {@link UserAccount}.
 * @author Maryna Kontar
 */
@Component
public class UserAccountDtoConverter extends AbstractDtoConverter<UserAccount, UserAccountDto> {
    @Override
    protected UserAccountDto createNewDto() {
        return new UserAccountDto();
    }

    @Override
    protected UserAccount createNewEntity() {
        return new UserAccount();
    }

    @Override
    protected void convertFromEntity(UserAccount entity, UserAccountDto dto) {
        UserDtoConverter userDtoConverter = new UserDtoConverter();
        SimpleUserDto simpleUserDto = userDtoConverter.transform(entity.getUser());
        dto.setUser(simpleUserDto);
        dto.setAccountType(entity.getAccountType());
        dto.setIsValueCompatibilityTestPassed(entity.getIsValueCompatibilityTestPassed());
        dto.setInviteTokens(entity.getInviteTokens());
        dto.setUsersForMatching(userDtoConverter.transform(entity.getUsersForMatching()));
        dto.setUsersWhoInvitedYou(userDtoConverter.transform(entity.getUsersWhoInvitedYou()));
        dto.setUsersWhoYouInvite(userDtoConverter.transform(entity.getUsersWhoYouInvite()));
    }

    @Override
    protected void convertFromDto(UserAccountDto dto, UserAccount entity) {
        UserDtoConverter userDtoConverter = new UserDtoConverter();
        User user = userDtoConverter.transform(dto.getUser());
        entity.setUser(user);
        entity.setAccountType(dto.getAccountType());
        entity.setUsersWhoInvitedYou(userDtoConverter.transform(dto.getUsersWhoInvitedYou()));
        entity.setUsersWhoYouInvite(userDtoConverter.transform(dto.getUsersWhoYouInvite()));
    }
}

