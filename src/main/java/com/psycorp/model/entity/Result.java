package com.psycorp.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity data level for saving data in database.
 * Numeric result of matching.
 * Embedded class for {@link MatchEntity}
 * @author Maryna Kontar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    private Double number;
}
