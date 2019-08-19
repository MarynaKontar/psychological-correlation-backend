package com.psycorp.model.entity;

import com.psycorp.model.enums.Area;
import com.psycorp.model.enums.MatchMethod;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Entity data level for saving data in database.
 * Result of matching for given area and matchMethod.
 * Embedded class for {@link UserMatchEntity}
 * @author Maryna Kontar
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Document
public class MatchEntity extends AbstractEntity{
    @NotNull
    private MatchMethod matchMethod;
    @NotNull
    private Area area;
    @NotNull @Valid
    private Result result;
}
