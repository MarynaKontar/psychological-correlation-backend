package com.psycorp.model.dto;

import com.psycorp.model.enums.Scale;
import lombok.Data;

@Data
public class ScaleDto {
    private Scale scale;
    private String scaleName;
}
