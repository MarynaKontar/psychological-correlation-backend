package com.psycorp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * DTO (Data Transfer Object) data level.
 * Embedded class for {@link ValueProfileDto}.
 * @author Vitaliy Proskura
 * @author Maryna Kontar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValueProfileElementDto {

    @NotEmpty
    private String scaleName;
    @NotNull
    private Double percentResult;
}
