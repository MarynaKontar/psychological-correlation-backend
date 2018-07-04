package com.psycorp.сonverter;

import com.psycorp.exception.BadRequestException;
import com.psycorp.model.dto.UserMatchDto;
import com.psycorp.model.entity.User;
import com.psycorp.model.entity.UserMatch;
import com.psycorp.service.UserService;
import com.psycorp.util.Advice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@PropertySource("classpath:errormessages.properties")
public class UserMatchDtoConverter extends AbstractDtoConverter<UserMatch, UserMatchDto> {

    private final UserDtoConverter userDtoConverter;
    private final UserService userService;
    private final Environment env;

    @Autowired
    public UserMatchDtoConverter(UserDtoConverter userDtoConverter, UserService userService, Environment env) {
        this.userDtoConverter = userDtoConverter;
        this.userService = userService;
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
    protected void convertFromEntity(UserMatch entity, UserMatchDto dto) {
        if(entity == null || dto == null) throw new BadRequestException(env.getProperty("error.UserMatchCan`tBeNull"));

        UserDtoConverter userDtoConverter = new UserDtoConverter(env);
//        dto.setId(entity.getId());
        dto.setMatches(entity.getMatches());
//        dto.setMatchMethod(entity.getMatchMethod());

        dto.setUsers(userDtoConverter.transform(entity.getUsers()));
//        dto.setUserNames(entity.getUserNames());
        dto.setAdvice(Advice.getAdvice(entity));
    }

    //TODO не используется так как для сравнения в контроллере просто используются имена пользователей, а @RequestBody не передается
    @Override
    protected void convertFromDto(UserMatchDto dto, UserMatch entity) {
        if(entity == null || dto == null) throw new BadRequestException(env.getProperty("error.UserMatchCan`tBeNull"));
        if(dto.getUsers().stream()
                .allMatch(simpleUserDto -> userService.findFirstUserByName(simpleUserDto.getName()) == null))
        { throw new BadRequestException(env.getProperty("error.noUserFind")); }

            Set<User> users = new HashSet<>(userDtoConverter.transform(dto.getUsers()));
            entity.setUsers(users);
            entity.setMatches(dto.getMatches());

    }
}
