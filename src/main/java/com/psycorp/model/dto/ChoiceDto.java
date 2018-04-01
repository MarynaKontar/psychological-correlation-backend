package com.psycorp.model.dto;

import lombok.Data;

@Data
public class ChoiceDto extends AbstractDto {
    private AreaDto area;
    private ScaleDto firstScale;
    private ScaleDto secondScale;
    private ScaleDto chosenScale;
}
