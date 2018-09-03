package com.psycorp.model.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class ChoiceDto extends AbstractDto {
    @NotEmpty
    private AreaDto area;
    @NotEmpty
    private ScaleDto firstScale;
    @NotEmpty
    private ScaleDto secondScale;
    @NotEmpty
    private ScaleDto chosenScale;
}
