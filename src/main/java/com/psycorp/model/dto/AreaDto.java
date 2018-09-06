package com.psycorp.model.dto;

import com.psycorp.model.enums.Area;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class AreaDto extends AbstractDto {
    @NotNull
    private Area area;
    @NotEmpty
    private String areaName;
    @NotEmpty
    private String areaQuestion;//можно это поле отправить в ChoiceDto
}
