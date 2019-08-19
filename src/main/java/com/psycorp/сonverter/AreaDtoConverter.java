package com.psycorp.сonverter;

import com.psycorp.exception.BadRequestException;
import com.psycorp.model.dto.AreaDto;
import com.psycorp.model.enums.Area;
import org.springframework.core.env.Environment;

/**
 * Dto converter for {@link Area}.
 * Area cant't extends from AbstractEntity (Area is enum)
 * so AreaDtoConverter can't extends AbstractDtoConverter like another dto converters.
 */
class AreaDtoConverter  {

    private final Environment env;

    AreaDtoConverter(Environment env) {
        this.env = env;
    }

    protected AreaDto createNewDto() {
        return new AreaDto();
    }

    protected void convertFromEntity(Area entity, AreaDto dto) {
        String areaName = entity.toString().toLowerCase() + ".area.name";//"goal.area.name"
        dto.setArea(entity);
        dto.setAreaName(env.getProperty(areaName));
    }

    protected Area convertFromDto(AreaDto dto) {
        if(dto == null) throw new BadRequestException(env.getProperty("error.AreaCan`tBeNull"));
        return dto.getArea();
    }
}
