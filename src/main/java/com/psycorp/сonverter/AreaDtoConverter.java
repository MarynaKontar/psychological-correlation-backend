package com.psycorp.сonverter;

import com.psycorp.model.dto.AreaDto;
import com.psycorp.model.enums.Area;

public class AreaDtoConverter  {


    protected AreaDto createNewDto() {
        return new AreaDto();
    }

    protected void convertFromEntity(Area entity, AreaDto dto) {
        dto.setArea(entity);
        dto.setAreaName(entity.name());
    }

}
