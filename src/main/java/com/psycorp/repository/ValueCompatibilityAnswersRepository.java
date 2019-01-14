package com.psycorp.repository;

import com.psycorp.model.entity.ValueCompatibilityAnswersEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RepositoryRestResource(collectionResourceRel = "valueCompatibilityAnswersEntity", path = "valueCompatibilityAnswersEntity")
public interface ValueCompatibilityAnswersRepository extends MongoRepository<ValueCompatibilityAnswersEntity, ObjectId> {

//    Optional<ValueCompatibilityAnswersEntity> findByUserIdOrderByIdDesc(ObjectId userId);

    //TODO сейчас не сработает, т.к. User хоть и не встроен в ValueCompatibilityAnswersEntity (@DBRef - true linking), но в User primary key теперь не name, а id
//    List<ValueCompatibilityAnswersEntity> findAllByUser_NameOrderByPassDateDesc(String name);

    //должен быть такой же как  findAllByUserIdOrderByPassDateDesc, так как ObjectId  ("OrderById") отсортирован по дате создания
    //правда с точностью 1 сек. Но наверное findAllByUserIdOrderByPassDateDesc будет использовать сортировку
    // в памяти, а  findAllByUser_NameOrderByIdDesc нет
    Optional<List<ValueCompatibilityAnswersEntity>> findAllByUser_IdOrderByIdDesc(ObjectId userId);
    Optional<ValueCompatibilityAnswersEntity> findTopByUser_IdOrderByPassDateDesc(ObjectId userId);

    List<ValueCompatibilityAnswersEntity> findAllByUser_IdAndPassedOrderByPassDateDesc(ObjectId userId, Boolean passed);

    @Query("{$find: {'user.$id': ?0, 'passed': ?1}, $sort: {'passDate': -1}, $limit: 1}")
    ValueCompatibilityAnswersEntity findAllByUser_IdAndPassedAndLastPassDate(ObjectId userId, Boolean passed);

    Optional<ValueCompatibilityAnswersEntity> findTopByUser_IdAndPassedOrderByPassDateDesc(ObjectId userId, Boolean passed);

//    Optional<ValueCompatibilityAnswersEntity> findFirstByUser_NameOrderByIdDesc(String name);
//    Optional<List<ValueCompatibilityAnswersEntity>> findAllByUser_NameOrderByIdDesc(String name);

//    void removeAllByUser_Id (ObjectId userId);
    void removeAllByUserId (ObjectId userId);
}