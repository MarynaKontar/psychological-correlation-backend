package com.psycorp.сonverter;

import com.psycorp.model.dto.SimpleUserDto;
import com.psycorp.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class UserDtoConverter extends AbstractDtoConverter<User, SimpleUserDto>{


    private final Environment env;

    @Autowired
    public UserDtoConverter(Environment env) {
        this.env = env;
    }

    @Override
    protected SimpleUserDto createNewDto() {
        return new SimpleUserDto();
    }

    @Override
    protected User createNewEntity() {
        return new User();
    }

    @Override
    protected void convertFromEntity(User entity, SimpleUserDto dto) {
//        if(entity == null || dto == null) throw new BadRequestException(env.getProperty("error.UserCan`tBeNull"));
        dto.setName(entity.getName());
        dto.setEmail(entity.getEmail());
        dto.setId(entity.getId());

    }

    @Override
    protected void convertFromDto(SimpleUserDto dto, User entity) {
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        if(dto.getId() != null) {
            entity.setId(dto.getId());
        }
    }
}
