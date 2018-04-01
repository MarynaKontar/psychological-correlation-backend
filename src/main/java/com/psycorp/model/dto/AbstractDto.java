package com.psycorp.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class AbstractDto {
    private Long id;
}
