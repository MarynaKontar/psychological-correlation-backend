package com.psycorp.model.dto;

import com.psycorp.model.enums.Scale;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValueProfileDto {
    private Scale scale;
    private String scaleName;
    private Double percentResult;
}
