package com.psycorp.сonverter;

import com.psycorp.model.dto.CredentialsDto;
import com.psycorp.model.entity.User;
import com.psycorp.model.security.CredentialsEntity;
import org.springframework.stereotype.Component;

@Component
public class CredentialsEntityConverter extends AbstractDtoConverter<CredentialsEntity, CredentialsDto> {

    @Override
    protected CredentialsDto createNewDto() {
        return new CredentialsDto();
    }

    @Override
    protected CredentialsEntity createNewEntity() {
        return new CredentialsEntity();
    }

    @Override
    protected void convertFromEntity(CredentialsEntity entity, CredentialsDto dto) {

        dto.setId(entity.getId());
        dto.setName(entity.getUser().getName());
        dto.setEmail(entity.getUser().getEmail());
        dto.setPassword(entity.getPassword());
    }

    @Override
    protected void convertFromDto(CredentialsDto dto, CredentialsEntity entity) {
        entity.setId(dto.getId());
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        entity.setUser(user);
        entity.setPassword(dto.getPassword());
    }
}
