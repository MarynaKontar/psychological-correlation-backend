package com.psycorp.model.dto;

import com.psycorp.model.objects.ValueProfileMatching;
import com.psycorp.model.objects.ValuesDifferencesComment;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * DTO (Data Transfer Object) data level.
 * for hiding implementation details of {@link ValueProfileMatching}.
 * @author Maryna Kontar
 */
@Data
public class ValueProfileMatchingDto extends AbstractDto{

    @NotNull @Valid
    private List<ValueProfileDto> valueProfiles;

    @NotNull @Valid
    private List<ValuesDifferencesComment> valuesDifferencesComments;

}
