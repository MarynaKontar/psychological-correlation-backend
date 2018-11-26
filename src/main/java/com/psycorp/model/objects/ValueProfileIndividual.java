package com.psycorp.model.objects;

import com.psycorp.model.entity.AbstractEntity;
import com.psycorp.model.entity.Result;
import com.psycorp.model.enums.Scale;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Data
public class ValueProfileIndividual extends AbstractEntity {

    @NotNull @Valid
    private ValueProfile valueProfile;

    @NotNull
    private List<ValueProfileComment> valueProfileCommentList;

//    private Map<Scale, ValueProfileComment> valueProfileCommentMap;
//    @NotNull @Valid
//    private Map<Scale, Map<Result, ValueProfileComment>> scaleResult;
//    @NotNull
//    private Boolean isPrincipalUser;
}
