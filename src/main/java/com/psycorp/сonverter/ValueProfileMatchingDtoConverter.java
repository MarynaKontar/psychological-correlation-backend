package com.psycorp.сonverter;

import com.psycorp.exception.BadRequestException;
import com.psycorp.model.dto.ValueProfileMatchingDto;
import com.psycorp.model.objects.ValueProfileMatching;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ValueProfileMatchingDtoConverter extends AbstractDtoConverter<ValueProfileMatching, ValueProfileMatchingDto> {

    private final ValueProfileDtoConverter valueProfileDtoConverter;

    @Autowired
    public ValueProfileMatchingDtoConverter(ValueProfileDtoConverter valueProfileDtoConverter) {
        this.valueProfileDtoConverter = valueProfileDtoConverter;
    }

    @Override
    protected ValueProfileMatchingDto createNewDto() {
        return new ValueProfileMatchingDto();
    }

    @Override
    protected ValueProfileMatching createNewEntity() {
        return new ValueProfileMatching();
    }

    @Override
    protected void convertFromEntity(ValueProfileMatching entity, ValueProfileMatchingDto dto) {
            dto.setValueProfiles(valueProfileDtoConverter.transform(entity.getValueProfileList()));
            dto.setValuesDifferencesComments(entity.getValuesDifferencesCommentList());
    }

    @Override
    protected void convertFromDto(ValueProfileMatchingDto dto, ValueProfileMatching entity) {
        throw new BadRequestException("There is never convert from ValueProfileMatchingDto to ValueProfileMatching");
    }
}
