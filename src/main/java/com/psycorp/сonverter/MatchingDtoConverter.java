package com.psycorp.сonverter;

import com.psycorp.model.dto.MatchingDto;
import com.psycorp.model.objects.Matching;
import org.springframework.stereotype.Component;

@Component
public class MatchingDtoConverter extends AbstractDtoConverter<Matching, MatchingDto>{

    @Override
    protected MatchingDto createNewDto() {
        return new MatchingDto();
    }

    @Override
    protected void convertFromEntity(Matching entity, MatchingDto dto) {

        dto.setArea(entity.getArea());
        dto.setMatchMethod(entity.getMatchMethod());
        dto.setResult(entity.getResult());
        dto.setUserMatchComment(entity.getUserMatchComment());
    }


    // не используемые методы. Можно или кидать исключение или не наследоваться от AbstractDtoConverter, а просто прописать здесь методы для transform collection как в AbstractDtoConverter
    @Override
    protected Matching createNewEntity() {
        return new Matching();
    }
    @Override
    protected void convertFromDto(MatchingDto dto, Matching entity) {
        entity.setMatchMethod(dto.getMatchMethod());
        entity.setResult(dto.getResult());
        entity.setArea(dto.getArea());
    }
}
