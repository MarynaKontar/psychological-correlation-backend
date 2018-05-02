package com.psycorp.сonverter;

import com.psycorp.exception.BadRequestException;
import com.psycorp.model.dto.AreaDto;
import com.psycorp.model.dto.ChoiceDto;
import com.psycorp.model.dto.ScaleDto;
import com.psycorp.model.entity.Choice;

public class ChoiceDtoConverter extends AbstractDtoConverter<Choice, ChoiceDto>{
    @Override
    protected ChoiceDto createNewDto() {
        return new ChoiceDto();
    }

    @Override
    protected Choice createNewEntity() {
        return new Choice();
    }

    @Override
    protected void convertFromEntity(Choice entity, ChoiceDto dto) {

        AreaDtoConverter areaDtoConverter = new AreaDtoConverter();
        AreaDto areaDto = areaDtoConverter.createNewDto();
        areaDtoConverter.convertFromEntity(entity.getArea(), areaDto);

        dto.setArea(areaDto);


        ScaleDtoConverter scaleDtoConverter = new ScaleDtoConverter();
        ScaleDto firstScaleDto = scaleDtoConverter.createNewDto();
        ScaleDto secondScaleDto = new ScaleDto();
        ScaleDto chosenScaleDto = new ScaleDto();
        scaleDtoConverter.convertFromEntity(entity.getFirstScale(), firstScaleDto);
        scaleDtoConverter.convertFromEntity(entity.getSecondScale(), secondScaleDto);
        scaleDtoConverter.convertFromEntity(entity.getChosenScale(), chosenScaleDto);

        dto.setFirstScale(firstScaleDto);
        dto.setSecondScale(secondScaleDto);
        dto.setChosenScale(chosenScaleDto);
    }

    @Override
    protected void convertFromDto(ChoiceDto dto, Choice entity) {
        throw  new BadRequestException("!!!!!НЕ НАПИСАН МЕТОД convertFromDto для Choice");
    }
}
