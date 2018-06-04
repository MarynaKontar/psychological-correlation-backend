package com.psycorp.сonverter;

import com.psycorp.exception.BadRequestException;
import com.psycorp.model.dto.ScaleDto;
import com.psycorp.model.enums.Area;
import com.psycorp.model.enums.Scale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:errormessages.properties")
public class ScaleDtoConverter {

    private final Environment env;

    @Autowired
    protected ScaleDtoConverter(Environment env) {
        this.env = env;
    }

    protected ScaleDto createNewDto() {
        return new ScaleDto();
    }

//    protected void convertFromEntity(Scale entity, ScaleDto dto) {
//        dto.setScale(entity);
//        dto.setScaleName(entity.name());
//    }

    protected void convertFromEntity(Scale entity, Area area, ScaleDto dto) {
        if(entity == null || dto == null || area == null) return;
//            throw new BadRequestException(env.getProperty("error.ScaleAndAreaCan`tBeNull"));

        String scaleName = area.toString().toLowerCase() + "." + entity.toString().toLowerCase();//goal.one
        dto.setScale(entity);
        dto.setScaleName(env.getProperty(scaleName));
    }

}
