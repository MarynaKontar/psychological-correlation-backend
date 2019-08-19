package com.psycorp.repository;

import com.psycorp.model.entity.ValueCompatibilityAnswersEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RepositoryRestResource(collectionResourceRel = "valueCompatibilityAnswersEntity", path = "valueCompatibilityAnswersEntity")
public interface ValueCompatibilityAnswersRepository extends MongoRepository<ValueCompatibilityAnswersEntity, ObjectId> {

    //должен быть такой же как  findAllByUserIdOrderByPassDateDesc, так как ObjectId  ("OrderById") отсортирован по дате создания
    //правда с точностью 1 сек. Но наверное findAllByUserIdOrderByPassDateDesc будет использовать сортировку
    // в памяти, а  findAllByUser_NameOrderByIdDesc нет
    Optional<List<ValueCompatibilityAnswersEntity>> findAllByUserIdOrderByIdDesc(ObjectId userId);
    Optional<ValueCompatibilityAnswersEntity> findTopByUserIdOrderByPassDateDesc(ObjectId userId);
    List<ValueCompatibilityAnswersEntity> findAllByUserIdAndPassedOrderByPassDateDesc(ObjectId userId, Boolean passed);
    Optional<ValueCompatibilityAnswersEntity> findTopByUserIdAndPassedOrderByPassDateDesc(ObjectId userId, Boolean passed);

    Boolean existsByUserIdAndPassed(ObjectId userId, Boolean passed);

    void removeAllByUserId (ObjectId userId);
}
