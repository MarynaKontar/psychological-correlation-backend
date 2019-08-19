package com.psycorp.model.dto;

import com.psycorp.model.enums.Scale;
import lombok.Data;

/**
 * DTO (Data Transfer Object) data level
 * for adding additional information to {@link Scale}.
 * Embedded class for {@link ChoiceDto}.
 * @author Vitaliy Proskura
 * @author Maryna Kontar
 */
@Data
public class ScaleDto extends AbstractDto
{
    private Scale scale;
    private String scaleHeader;
    private String scaleDescription;
}
