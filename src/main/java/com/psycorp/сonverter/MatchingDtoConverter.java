package com.psycorp.сonverter;

import com.psycorp.exception.BadRequestException;
import com.psycorp.model.dto.MatchingDto;
import com.psycorp.model.objects.Matching;
import org.springframework.stereotype.Component;

/**
 * Dto converter for {@link Matching}.
 */
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

    @Override
    protected Matching createNewEntity() {
        throw new BadRequestException("Never creates new Matching in MatchingDtoConverter");
    }
    @Override
    protected void convertFromDto(MatchingDto dto, Matching entity) {
        throw new BadRequestException("Never converts from MatchingDto to Matching");
    }
}
