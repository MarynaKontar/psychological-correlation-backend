package com.psycorp.model.dto;

import com.psycorp.model.enums.Area;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class AreaDto extends AbstractDto
{
    @NotEmpty
    private Area area;
    @NotEmpty
    private String areaName;
    @NotEmpty
    private String areaQuestion;//можно это поле отправить в ChoiceDto


}
