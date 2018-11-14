package com.psycorp.model.entity;

import com.psycorp.model.enums.Area;
import com.psycorp.model.enums.MatchMethod;
import lombok.Data;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class Match extends AbstractEntity{
    @NotNull
    private MatchMethod matchMethod;
    @NotNull
    private Area area;
    @NotNull @Valid
    private Result result;
}
