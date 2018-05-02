package com.psycorp.сonverter;

import com.psycorp.model.dto.ScaleDto;
import com.psycorp.model.enums.Scale;

public class ScaleDtoConverter {

    protected ScaleDto createNewDto() {
        return new ScaleDto();
    }

    protected void convertFromEntity(Scale entity, ScaleDto dto) {
        dto.setScale(entity);
        dto.setScaleName(entity.name());
    }

}
