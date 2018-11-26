package com.psycorp.сonverter;

import com.psycorp.exception.BadRequestException;
import com.psycorp.model.dto.ValueProfileIndividualDto;
import com.psycorp.model.objects.ValueProfileIndividual;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValueProfileIndividualDtoConverter extends AbstractDtoConverter<ValueProfileIndividual, ValueProfileIndividualDto>{

    private final ValueProfileDtoConverter valueProfileDtoConverter;

    @Autowired
    public ValueProfileIndividualDtoConverter(ValueProfileDtoConverter valueProfileDtoConverter) {
        this.valueProfileDtoConverter = valueProfileDtoConverter;
    }

    @Override
    protected ValueProfileIndividualDto createNewDto() {
        return new ValueProfileIndividualDto();
    }

    @Override
    protected ValueProfileIndividual createNewEntity() {
        return new ValueProfileIndividual();
    }

    @Override
    protected void convertFromEntity(ValueProfileIndividual entity, ValueProfileIndividualDto dto) {

        dto.setValueProfile(valueProfileDtoConverter.transform(entity.getValueProfile()));
        dto.setValueProfileComments(entity.getValueProfileCommentList());
    }

    @Override
    protected void convertFromDto(ValueProfileIndividualDto dto, ValueProfileIndividual entity) {
        throw new BadRequestException("There is never convert from ValueProfileIndividualDto to ValueProfileIndividual");
    }
}
