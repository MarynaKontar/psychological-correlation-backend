package com.psycorp.model.dto;

import com.psycorp.model.entity.Choice;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * DTO (Data Transfer Object) data level
 * for hiding implementation details of {@link Choice}.
 * Embedded class for {@link ValueCompatibilityAnswersDto}.
 * @author Vitaliy Proskura
 * @author Maryna Kontar
 */
@Data
public class ChoiceDto extends AbstractDto {
    @NotNull @Valid
    private AreaDto area;
    @NotEmpty
    private String question;
    @NotNull @Valid
    private ScaleDto firstScale;
    @NotNull @Valid
    private ScaleDto secondScale;
    @NotNull
    private ScaleDto chosenScale;
}
