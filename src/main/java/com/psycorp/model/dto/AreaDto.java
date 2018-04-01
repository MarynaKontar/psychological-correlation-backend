package com.psycorp.model.dto;

import com.psycorp.model.enums.Area;
import lombok.Data;

@Data
public class AreaDto {
    private Area area;
    private String areaName;
}
