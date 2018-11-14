package com.psycorp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValueProfileDto {

    @NotNull @Valid
    private List<ValueProfileElementDto> valueProfileElements;

    private Boolean isPrincipalUser;
}
