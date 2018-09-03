package com.psycorp.сonverter;

import com.psycorp.exception.BadRequestException;
import com.psycorp.model.dto.ScaleDto;
import com.psycorp.model.enums.Area;
import com.psycorp.model.enums.Scale;
import org.springframework.core.env.Environment;

class ScaleDtoConverter {

    private final Environment env;

    ScaleDtoConverter(Environment env) {
        this.env = env;
    }

    protected ScaleDto createNewDto() {
        return new ScaleDto();
    }

    protected void convertFromEntity(Scale entity, Area area, ScaleDto dto) {
        if(area == null) throw new BadRequestException(env.getProperty("error.AreaCan`tBeNull"));
        if(entity == null || dto == null) return; //chosenScale can be null, so "return"

        String scaleName = area.toString().toLowerCase() + "." + entity.toString().toLowerCase();//goal.one
        dto.setScale(entity);
        dto.setScaleName(env.getProperty(scaleName));
    }

    protected Scale convertFromDto(ScaleDto dto) {
        if(dto == null) return null;
        return dto.getScale();
    }
}
