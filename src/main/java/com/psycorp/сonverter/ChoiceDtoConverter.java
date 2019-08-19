package com.psycorp.сonverter;

import com.psycorp.model.dto.AreaDto;
import com.psycorp.model.dto.ChoiceDto;
import com.psycorp.model.dto.ScaleDto;
import com.psycorp.model.entity.Choice;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Dto converter for {@link Choice}.
 */
@Component
public class ChoiceDtoConverter extends AbstractDtoConverter<Choice, ChoiceDto>{

    private final Environment env;

    ChoiceDtoConverter(Environment env) {
        this.env = env;
    }

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

        AreaDtoConverter areaDtoConverter = new AreaDtoConverter(env);
        AreaDto areaDto = areaDtoConverter.createNewDto();
        areaDtoConverter.convertFromEntity(entity.getArea(), areaDto);

        ScaleDtoConverter scaleDtoConverter = new ScaleDtoConverter(env);
        ScaleDto firstScaleDto = scaleDtoConverter.createNewDto();
        ScaleDto secondScaleDto = scaleDtoConverter.createNewDto();
        ScaleDto chosenScaleDto = scaleDtoConverter.createNewDto();
        scaleDtoConverter.convertFromEntity(entity.getFirstScale(), entity.getArea(), firstScaleDto);
        scaleDtoConverter.convertFromEntity(entity.getSecondScale(), entity.getArea(), secondScaleDto);
        scaleDtoConverter.convertFromEntity(entity.getChosenScale(), entity.getArea(), chosenScaleDto);

        String areaQuestion = entity.getArea().toString()
                .toLowerCase() + ".question";//"goal.question"

        dto.setQuestion(env.getProperty(areaQuestion));
        dto.setArea(areaDto);
        dto.setFirstScale(firstScaleDto);
        dto.setSecondScale(secondScaleDto);
        dto.setChosenScale(chosenScaleDto);
    }

    @Override
    protected void convertFromDto(ChoiceDto dto, Choice entity) {

        entity.setArea(dto.getArea().getArea());
        entity.setFirstScale(dto.getFirstScale().getScale());
        entity.setSecondScale(dto.getSecondScale().getScale());
        entity.setChosenScale(dto.getChosenScale().getScale());
    }
}
