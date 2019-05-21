package com.psycorp.model.entity;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Set;

/**
 * Расчитанные результаты совместимости двух (или больше) пользователей
 */
@Data
@Document
//@CompoundIndexes({
//        @CompoundIndex(name = "users.$id", def = "{'users.$id' : 1}"),
//        @CompoundIndex(name = "matches.result.number", def = "{'matches.result.number' : -1}")})

public class UserMatchEntity extends AbstractEntity
{
    @Id
    private ObjectId id;
    private Set<ObjectId> usersId;
    private List<MatchEntity> matches;
}
