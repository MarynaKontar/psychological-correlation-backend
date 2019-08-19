package com.psycorp.model.dto;

import com.psycorp.model.enums.Area;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * DTO (Data Transfer Object) data level
 * for adding additional information to {@link Area}.
 * Embedded class for {@link ChoiceDto}.
 * @author Vitaliy Proskura
 * @author Maryna Kontar
 */
@Data
public class AreaDto extends AbstractDto {
    @NotNull
    private Area area;
    @NotEmpty
    private String areaName;
}
