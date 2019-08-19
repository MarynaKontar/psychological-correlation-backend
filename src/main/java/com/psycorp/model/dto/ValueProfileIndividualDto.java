package com.psycorp.model.dto;

import com.psycorp.model.objects.ValueProfileComment;
import com.psycorp.model.objects.ValueProfileIndividual;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * DTO (Data Transfer Object) data level
 * for hiding implementation details of {@link ValueProfileIndividual}.
 * @author Maryna Kontar
 */
@Data
public class ValueProfileIndividualDto extends AbstractDto{
    @NotNull @Valid
    private ValueProfileDto valueProfile;

    @NotNull @Valid
    private List<ValueProfileComment> valueProfileComments;
}
