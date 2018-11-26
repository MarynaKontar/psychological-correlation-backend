package com.psycorp.model.dto;

import com.psycorp.model.objects.ValuesDifferencesComment;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ValueProfileMatchingDto extends AbstractDto{

    @NotNull @Valid
    private List<ValueProfileDto> valueProfiles;

    @NotNull @Valid
    private List<ValuesDifferencesComment> valuesDifferencesComments;

}
