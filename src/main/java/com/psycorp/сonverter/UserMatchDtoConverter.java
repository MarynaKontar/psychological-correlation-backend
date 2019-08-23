package com.psycorp.сonverter;

import com.psycorp.exception.BadRequestException;
import com.psycorp.model.dto.UserMatchDto;
import com.psycorp.model.objects.UserMatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Dto converter for {@link UserMatch}.
 * @author Maryna Kontar
 */
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
    protected void convertFromEntity(UserMatch entity, UserMatchDto dto) {
        dto.setMatches(matchingDtoConverter.transform(entity.getMatches()));
        dto.setUsers(userDtoConverter.transform(entity.getUsers()));
        dto.setId(entity.getId());
    }

    @Override
    protected UserMatch createNewEntity() {
        throw new BadRequestException("Never creates new UserMatch in UserMatchDtoConverter");
    }

    @Override
    protected void convertFromDto(UserMatchDto dto, UserMatch entity) {
        throw new BadRequestException("Never converts from UserMatchDto to UserMatch");
    }
}
