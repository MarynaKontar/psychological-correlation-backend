package com.psycorp.сonverter;

import com.psycorp.model.dto.UserMatchDto;
import com.psycorp.model.entity.User;
import com.psycorp.model.objects.UserMatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class UserMatchDtoConverter extends AbstractDtoConverter<UserMatch, UserMatchDto> {

    private final MatchingDtoConverter matchingDtoConverter;
    private final UserDtoConverter userDtoConverter;

    @Autowired
    public UserMatchDtoConverter(MatchingDtoConverter matchingDtoConverter, UserDtoConverter userDtoConverter) {
        this.matchingDtoConverter = matchingDtoConverter;
        this.userDtoConverter = userDtoConverter;
    }

    @Override
    protected UserMatchDto createNewDto() {
        return new UserMatchDto();
    }

    @Override
    protected UserMatch createNewEntity() {
        return new UserMatch();
    }

    @Override
    protected void convertFromEntity(UserMatch entity, UserMatchDto dto) {
//        UserDtoConverter userDtoConverter = new UserDtoConverter(env);
        dto.setMatches(matchingDtoConverter.transform(entity.getMatches()));
        dto.setUsers(userDtoConverter.transform(entity.getUsers()));
        dto.setId(entity.getId());
    }

    //TODO переделать
    @Override
    protected void convertFromDto(UserMatchDto dto, UserMatch entity) {
            Set<User> users = new HashSet<>(userDtoConverter.transform(dto.getUsers()));
            entity.setUsers(users);
            entity.setMatches(matchingDtoConverter.transform(dto.getMatches()));

    }
}
