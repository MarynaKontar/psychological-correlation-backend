package com.psycorp.сonverter;

import com.psycorp.model.dto.SimpleUserDto;
import com.psycorp.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Dto converter for {@link User}.
 * @author Vitaliy Proskura
 * @author Maryna Kontar
 */
@Component
public class UserDtoConverter extends AbstractDtoConverter<User, SimpleUserDto>{

    @Autowired
    public UserDtoConverter() { }

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
        dto.setName(entity.getName());
        dto.setEmail(entity.getEmail());
        dto.setAge(entity.getAge());
        dto.setGender(entity.getGender());
        dto.setId(entity.getId());
    }

    @Override
    protected void convertFromDto(SimpleUserDto dto, User entity) {
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setAge(dto.getAge());
        entity.setGender(dto.getGender());
        if(dto.getId() != null) {
            entity.setId(dto.getId());
        }
    }
}
