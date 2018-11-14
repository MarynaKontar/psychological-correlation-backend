package com.psycorp.сonverter;

import com.psycorp.model.dto.UserMatchDto;
import com.psycorp.model.entity.User;
import com.psycorp.model.entity.UserMatch;
import com.psycorp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class UserMatchDtoConverter extends AbstractDtoConverter<UserMatch, UserMatchDto> {

    private final MatchDtoConverter matchDtoConverter;
    private final Environment env;

    @Autowired
    public UserMatchDtoConverter(MatchDtoConverter matchDtoConverter, Environment env) {
        this.matchDtoConverter = matchDtoConverter;
        this.env = env;
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
    //TODO убрать exceptions!!!!!!!!!!! Заменить на valid
    protected void convertFromEntity(UserMatch entity, UserMatchDto dto) {
//        if(entity == null || dto == null) throw new BadRequestException(env.getProperty("error.UserMatchCan`tBeNull"));

        UserDtoConverter userDtoConverter = new UserDtoConverter(env);
//        dto.setId(entity.getId());
        dto.setMatches(matchDtoConverter.transform(entity.getMatches()));
//        dto.setMatchMethod(entity.getMatchMethod());

        dto.setUsers(userDtoConverter.transform(entity.getUsers()));
//        dto.setUserNames(entity.getUserNames());
//        dto.setAdvice(UserMatchCommentUtil.getComment(entity));
        dto.setId(entity.getId());
    }

    //TODO переделать
    @Override
    protected void convertFromDto(UserMatchDto dto, UserMatch entity) {
//        if(entity == null || dto == null) throw new BadRequestException(env.getProperty("error.UserMatchCan`tBeNull"));
//        if(dto.getUsers().stream()
//                .allMatch(simpleUserDto -> userService.findFirstUserByName(simpleUserDto.getName()) == null))
//        { throw new BadRequestException(env.getProperty("error.noUserFound")); }
            UserDtoConverter userDtoConverter = new UserDtoConverter(env);
            Set<User> users = new HashSet<>(userDtoConverter.transform(dto.getUsers()));
            entity.setUsers(users);
            entity.setMatches(matchDtoConverter.transform(dto.getMatches()));

    }
}
