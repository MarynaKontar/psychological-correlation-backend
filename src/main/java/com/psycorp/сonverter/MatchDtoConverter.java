package com.psycorp.сonverter;

import com.psycorp.model.dto.MatchDto;
import com.psycorp.model.entity.Match;
import com.psycorp.util.UserMatchCommentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class MatchDtoConverter extends AbstractDtoConverter<Match, MatchDto>{

    private final Environment env;

    @Autowired
    public MatchDtoConverter(Environment env) {
        this.env = env;
    }

    @Override
    protected MatchDto createNewDto() {
        return new MatchDto();
    }

    @Override
    protected void convertFromEntity(Match entity, MatchDto dto) {
        dto.setArea(entity.getArea());
        dto.setMatchMethod(entity.getMatchMethod());
        dto.setResult(entity.getResult());
        dto.setUserMatchComment(UserMatchCommentUtil.getComment(entity, env));
    }


    // не используемые методы. Можно или кидать исключение или не наследоваться от AbstractDtoConverter, а просто прописать здесь методы для transform collection как в AbstractDtoConverter
    @Override
    protected Match createNewEntity() {
        return new Match();
    }
    @Override
    protected void convertFromDto(MatchDto dto, Match entity) {
        entity.setMatchMethod(dto.getMatchMethod());
        entity.setResult(dto.getResult());
        entity.setArea(dto.getArea());
    }
}
