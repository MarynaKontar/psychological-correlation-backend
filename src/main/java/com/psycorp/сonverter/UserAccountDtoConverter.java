package com.psycorp.сonverter;

import com.psycorp.model.dto.SimpleUserDto;
import com.psycorp.model.dto.UserAccountDto;
import com.psycorp.model.entity.User;
import com.psycorp.model.objects.UserAccount;
import org.springframework.stereotype.Component;

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
        dto.setIsValueCompatibilityTestPassed(entity.getIsValueCompatibilityTestPassed());
        dto.setInviteTokens(entity.getInviteTokens());

    }

    @Override
    protected void convertFromDto(UserAccountDto dto, UserAccount entity) {
        UserDtoConverter userDtoConverter = new UserDtoConverter();
        User user = userDtoConverter.transform(dto.getUser());
        entity.setUser(user);
        //TODO когда акккаунт приходит измененным с фронта, то мы проверяем измененные поля и меняем их в бд,
        // но InviteTokens и IsValueCompatibilityTestPassed так не меняются, поєтому не конвертируем их.
        // Возможно name и email тоже не надо, так как для их изменения будет отдельная функция

//        entity.setInviteTokens(dto.getInviteTokens());
//        entity.setIsValueCompatibilityTestPassed(dto.getIsValueCompatibilityTestPassed());
    }
}

