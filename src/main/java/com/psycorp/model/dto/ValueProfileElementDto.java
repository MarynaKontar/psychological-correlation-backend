package com.psycorp.model.dto;

import com.psycorp.model.objects.ValueProfileComment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValueProfileElementDto {

    @NotEmpty
    private String scaleName;
    @NotNull
    private Double percentResult;
//    @NotNull
//    private ValueProfileComment comment;
}