package com.psycorp.model.dto;

import lombok.Data;
import org.bson.types.ObjectId;

@Data
public class ChoiceDto extends AbstractDto {
//    private ObjectId id;
    private AreaDto area;
    private ScaleDto firstScale;
    private ScaleDto secondScale;
    private ScaleDto chosenScale;
}
