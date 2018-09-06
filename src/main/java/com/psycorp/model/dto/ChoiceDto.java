package com.psycorp.model.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class ChoiceDto extends AbstractDto {
    @NotNull @Valid
    private AreaDto area;
    @NotNull @Valid
    private ScaleDto firstScale;
    @NotNull @Valid
    private ScaleDto secondScale;
    @NotNull
    private ScaleDto chosenScale;
}
