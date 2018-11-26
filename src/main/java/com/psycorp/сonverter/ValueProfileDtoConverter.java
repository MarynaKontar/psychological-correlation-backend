package com.psycorp.сonverter;

import com.psycorp.exception.BadRequestException;
import com.psycorp.model.dto.ValueProfileDto;
import com.psycorp.model.dto.ValueProfileElementDto;
import com.psycorp.model.objects.ValueProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ValueProfileDtoConverter extends AbstractDtoConverter<ValueProfile, ValueProfileDto>{

    private final Environment env;

    @Autowired
    public ValueProfileDtoConverter(Environment env) {
        this.env = env;
    }

//    public ValueProfileDto convertToValueProfileDto(ValueProfile valueProfile) {
//        List<ValueProfileElementDto> valueProfileElements = new ArrayList<>();
//
////        valueProfile.getScaleResult().forEach((scale, comment) ->
////                {
//////                    comment.setResult(comment.getResult() * 100);
////                    valueProfileElements.add(
////                            new ValueProfileElementDto(env.getProperty(scale.name()), comment.getResult(), comment)
////                    );
////                }
////        );
//
//        valueProfile.getScaleResult().forEach((scale, result) ->
//                {
////                    comment.setResult(comment.getResult() * 100);
//                    valueProfileElements.add(
//                            new ValueProfileElementDto(env.getProperty(scale.name()), result.getNumber(),
//                                    ValueProfileCommentUtil.getComment(env, scale, result.getNumber()))
//                    );
//                }
//        );
//        ValueProfileDto valueProfile = new ValueProfileDto();
//        valueProfile.setValueProfileElements(valueProfileElements);
//        valueProfile.setIsPrincipalUser(valueProfile.getIsPrincipalUser());
//        return valueProfile;
//    }

    @Override
    protected ValueProfileDto createNewDto() {
        return new ValueProfileDto();
    }

    @Override
    protected ValueProfile createNewEntity() {
        return new ValueProfile();
    }

    @Override
    protected void convertFromEntity(ValueProfile entity, ValueProfileDto dto) {
        List<ValueProfileElementDto> valueProfileElementDtos = new ArrayList<>();
        entity.getScaleResult().forEach((scale, result) -> valueProfileElementDtos.add(
                new ValueProfileElementDto(env.getProperty(scale.name()), result.getNumber()
//                        ,ValueProfileCommentUtil.getComment(env, scale, result.getNumber())
                ))

        );
        dto.setValueProfileElements(valueProfileElementDtos);
        dto.setIsPrincipalUser(entity.getIsPrincipalUser());
    }

    @Override
    protected void convertFromDto(ValueProfileDto dto, ValueProfile entity) {
        throw new BadRequestException("There is never convert from ValueProfileDto to ValueProfile");
    }
}
