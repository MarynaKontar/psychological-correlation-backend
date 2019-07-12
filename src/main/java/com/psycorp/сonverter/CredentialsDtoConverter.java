package com.psycorp.сonverter;

import com.psycorp.model.dto.CredentialsDto;
import com.psycorp.model.entity.User;
import com.psycorp.model.objects.Credentials;
import com.psycorp.model.security.CredentialsEntity;
import org.springframework.stereotype.Component;

@Component
public class CredentialsDtoConverter extends AbstractDtoConverter<Credentials, CredentialsDto> {

    @Override
    protected CredentialsDto createNewDto() {
        return new CredentialsDto();
    }

    @Override
    protected Credentials createNewEntity() {
        return new Credentials();
    }

    @Override
    protected void convertFromEntity(Credentials entity, CredentialsDto dto) {

        dto.setId(entity.getId());
        dto.setName(entity.getUser().getName());
        dto.setEmail(entity.getUser().getEmail());
        dto.setGender(entity.getUser().getGender());
        dto.setAge(entity.getUser().getAge());
        dto.setPassword(entity.getPassword());
    }

    @Override
    protected void convertFromDto(CredentialsDto dto, Credentials entity) {
        entity.setId(dto.getId());
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setGender(dto.getGender());
        user.setAge(dto.getAge());
        entity.setUser(user);
        entity.setPassword(dto.getPassword());
    }
}
