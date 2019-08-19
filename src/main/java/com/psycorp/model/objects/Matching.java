package com.psycorp.model.objects;

import com.psycorp.model.entity.AbstractEntity;
import com.psycorp.model.entity.Result;
import com.psycorp.model.entity.UserMatchEntity;
import com.psycorp.model.enums.Area;
import com.psycorp.model.enums.MatchMethod;
import com.psycorp.util.UserMatchCommentUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Object data level for collecting and transforming data from a database.
 * Contains comment {@link UserMatchComment} for {@link Area} depending on {@link Result}.
 * These {@link MatchMethod}, {@link Area}, {@link Result} collecting from {@link UserMatchEntity}.
 * {@link UserMatchComment} is added using {@link UserMatchCommentUtil}.
 * @author Maryna Kontar
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Matching extends AbstractEntity {
    @NotNull
    private MatchMethod matchMethod;
    @NotNull
    private Area area;
    @NotNull @Valid
    private Result result;
    @NotNull @Valid
    private UserMatchComment userMatchComment;
}
