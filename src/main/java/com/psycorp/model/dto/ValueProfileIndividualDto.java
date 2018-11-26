package com.psycorp.model.dto;

import com.psycorp.model.objects.ValueProfileComment;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ValueProfileIndividualDto extends AbstractDto{
    @NotNull @Valid
    private ValueProfileDto valueProfile;

    @NotNull @Valid
    private List<ValueProfileComment> valueProfileComments;
}
