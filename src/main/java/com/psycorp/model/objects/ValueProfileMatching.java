package com.psycorp.model.objects;

import com.psycorp.model.entity.AbstractEntity;
import com.psycorp.model.entity.User;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ValueProfileMatching extends AbstractEntity{

//    @NotNull @Valid
//    private List<User> users;

    @NotNull @Valid
    private List<ValueProfile> valueProfileList;

    @NotNull @Valid
    private List<ValuesDifferencesComment> valuesDifferencesCommentList;
    //    @NotNull @Valid
//    private Map<Scale, Map<Result, valuesDifferencesCommentList>> scaleResult;

}
