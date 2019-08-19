package com.psycorp.model.entity;

import lombok.Data;

/**
 * Entity data level for saving data in database.
 * Common abstract class for all entities.
 * (first of all it's needed to convert object to dto
 * and vise versa in AbstractDtoConverter)
 * @author Maryna Kontar
 * @author Vitaliy Proskura
 */
@Data
public abstract class AbstractEntity {
}
