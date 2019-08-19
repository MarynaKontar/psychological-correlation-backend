package com.psycorp.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Set;

/**
 * Entity data level for saving data in database.
 * Stores matching data for set of users in database.
 * Created by comparing data from {@link ValueCompatibilityAnswersEntity}
 * for set of users.
 * @author Maryna Kontar
 * @author Vitaliy Proskura
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Document
public class UserMatchEntity extends AbstractEntity
{
    @Id
    private ObjectId id;
    private Set<ObjectId> usersId;
    private List<MatchEntity> matches;
}
