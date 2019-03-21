package com.psycorp.model.dto;

import com.psycorp.model.enums.Scale;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class ScaleDto extends AbstractDto
{
//    @NotNull
    private Scale scale;
//    @NotEmpty
    private String scaleHeader;
    private String scaleDescription;
}
