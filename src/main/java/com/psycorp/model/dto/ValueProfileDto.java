package com.psycorp.model.dto;

import com.psycorp.model.objects.ValueProfile;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * DTO (Data Transfer Object) data level
 * for hiding implementation details of {@link ValueProfile}.
 * Embedded class for {@link ValueProfileIndividualDto} and {@link ValueProfileMatchingDto}
 * @author Maryna Kontar
 */
@Data
public class ValueProfileDto extends AbstractDto {

    @NotNull @Valid
    private List<ValueProfileElementDto> valueProfileElements;

    private Boolean isPrincipalUser;
}
