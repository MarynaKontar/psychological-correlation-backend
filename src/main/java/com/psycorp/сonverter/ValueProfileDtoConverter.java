package com.psycorp.сonverter;

import com.psycorp.exception.BadRequestException;
import com.psycorp.model.dto.ValueProfileDto;
import com.psycorp.model.dto.ValueProfileElementDto;
import com.psycorp.model.objects.ValueProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Dto converter for {@link ValueProfile}.
 * @author Maryna Kontar
 */
@Component
public class ValueProfileDtoConverter extends AbstractDtoConverter<ValueProfile, ValueProfileDto>{

    private final Environment env;

    @Autowired
    public ValueProfileDtoConverter(Environment env) {
        this.env = env;
    }

    @Override
    protected ValueProfileDto createNewDto() {
        return new ValueProfileDto();
    }

    @Override
    protected void convertFromEntity(ValueProfile entity, ValueProfileDto dto) {
        List<ValueProfileElementDto> valueProfileElementDtos = new ArrayList<>();
        entity.getScaleResult().forEach((scale, result) -> valueProfileElementDtos.add(
                new ValueProfileElementDto(env.getProperty(scale.name()), result.getNumber())));
        dto.setValueProfileElements(valueProfileElementDtos);
        dto.setIsPrincipalUser(entity.getIsPrincipalUser());
    }

    @Override
    protected ValueProfile createNewEntity() {
        throw new BadRequestException("Never creates new ValueProfile in ValueProfileDtoConverter");
    }

    @Override
    protected void convertFromDto(ValueProfileDto dto, ValueProfile entity) {
        throw new BadRequestException("Never convert from ValueProfileDto to ValueProfile");
    }
}
