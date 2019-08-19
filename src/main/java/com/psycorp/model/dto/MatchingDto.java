package com.psycorp.model.dto;

import com.psycorp.model.entity.Result;
import com.psycorp.model.enums.Area;
import com.psycorp.model.enums.MatchMethod;
import com.psycorp.model.objects.Matching;
import com.psycorp.model.objects.UserMatchComment;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * DTO (Data Transfer Object) data level
 * for hiding implementation details of {@link Matching}.
 * Embedded class for {@link ValueProfileIndividualDto} and {@link UserMatchDto}
 * @author Maryna Kontar
 */
@Data
public class MatchingDto extends AbstractDto{

    @NotNull
    private MatchMethod matchMethod;
    @NotNull
    private Area area;
    @NotNull @Valid
    private Result result;
    @NotNull @Valid
    private UserMatchComment userMatchComment;
}
