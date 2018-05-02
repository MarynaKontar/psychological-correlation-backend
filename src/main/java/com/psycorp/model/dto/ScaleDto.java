package com.psycorp.model.dto;

import com.psycorp.model.enums.Scale;
import lombok.Data;

@Data
public class ScaleDto
//        extends AbstractDto
{
    private Scale scale;
    private String scaleName;
}
