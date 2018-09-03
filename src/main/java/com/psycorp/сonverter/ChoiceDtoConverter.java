package com.psycorp.сonverter;

import com.psycorp.exception.BadRequestException;
import com.psycorp.model.dto.AreaDto;
import com.psycorp.model.dto.ChoiceDto;
import com.psycorp.model.dto.ScaleDto;
import com.psycorp.model.entity.Choice;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ChoiceDtoConverter extends AbstractDtoConverter<Choice, ChoiceDto>{

    //TODO если не передавать Environment через конструктор (а просто @Autowired), то почему-то
    // не подтягивает Environment (env=null)

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
        if(entity == null || dto == null) throw new BadRequestException(env.getProperty("error.ChoiceCan`tBeNull"));

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

        dto.setArea(areaDto);
        dto.setFirstScale(firstScaleDto);
        dto.setSecondScale(secondScaleDto);
        dto.setChosenScale(chosenScaleDto);
    }

    @Override
    protected void convertFromDto(ChoiceDto dto, Choice entity) {
        if(entity == null || dto == null) throw new BadRequestException(env.getProperty("error.ChoiceCan`tBeNull"));
        if(dto.getArea() == null || dto.getFirstScale() == null || dto.getSecondScale() == null)
            throw new BadRequestException(env.getProperty("error.ScaleAndAreaCan`tBeNull"));

        entity.setArea(dto.getArea().getArea());
        entity.setFirstScale(dto.getFirstScale().getScale());
        entity.setSecondScale(dto.getSecondScale().getScale());
        entity.setChosenScale(dto.getChosenScale().getScale());
    }
}
