package com.psycorp.сonverter;

import com.psycorp.model.dto.ScaleDto;
import com.psycorp.model.enums.Area;
import com.psycorp.model.enums.Scale;
import org.springframework.core.env.Environment;

/**
 * Dto converter for {@link Scale}.
 * Scale cant't extends from AbstractEntity (Scale is enum)
 * so ScaleDtoConverter can't extends AbstractDtoConverter like another dto converters.
 * @author Maryna Kontar
 */
class ScaleDtoConverter {

    private final Environment env;

    ScaleDtoConverter(Environment env) {
        this.env = env;
    }

    protected ScaleDto createNewDto() {
        return new ScaleDto();
    }

    protected void convertFromEntity(Scale entity, Area area, ScaleDto dto) {

        if(entity == null || dto == null) return; //chosenScale can be null, so "return"

        // TODO сделать объект ValueCompatibilityAnswers, и эту логику перенести в сервис
        String scaleHeader = area.toString().toLowerCase() + ".header." + entity.toString().toLowerCase();
        String scaleDescription = area.toString().toLowerCase() + "." + entity.toString().toLowerCase();//goal.one

        dto.setScale(entity);
        dto.setScaleHeader(env.getProperty(scaleHeader));
        dto.setScaleDescription(env.getProperty(scaleDescription));
    }

    protected Scale convertFromDto(ScaleDto dto) {
        if(dto == null) return null;
        return dto.getScale();
    }
}
