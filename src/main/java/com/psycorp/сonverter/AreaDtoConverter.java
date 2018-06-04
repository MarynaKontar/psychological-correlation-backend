package com.psycorp.сonverter;

import com.psycorp.exception.BadRequestException;
import com.psycorp.model.dto.AreaDto;
import com.psycorp.model.enums.Area;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:errormessages.properties")
public class AreaDtoConverter  {

    private final Environment env;

    @Autowired
    protected AreaDtoConverter(Environment env) {
        this.env = env;
    }

    protected AreaDto createNewDto() {
        return new AreaDto();
    }

    protected void convertFromEntity(Area entity, AreaDto dto) {
        if(entity == null || dto == null) throw new BadRequestException(env.getProperty("error.AreaCan`tBeNull"));

        String areaName = entity.toString().toLowerCase() + ".area.name";//"goal.area.name"
        String areaQuestion = entity.toString()
                .toLowerCase() + ".question";//"goal.question"
        dto.setArea(entity);
        dto.setAreaName(env.getProperty(areaName));
        dto.setAreaQuestion(env.getProperty(areaQuestion));
    }

    protected Area convertFromDto(AreaDto dto) {
        if(dto == null) throw new BadRequestException(env.getProperty("error.AreaCan`tBeNull"));
        return dto.getArea();
    }
}
