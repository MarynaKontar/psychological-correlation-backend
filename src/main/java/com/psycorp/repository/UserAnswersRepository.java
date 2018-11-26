package com.psycorp.repository;

import com.psycorp.model.entity.UserAnswersEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RepositoryRestResource(collectionResourceRel = "userAnswers", path = "userAnswers")
public interface UserAnswersRepository extends MongoRepository<UserAnswersEntity, ObjectId> {

//    Optional<UserAnswersEntity> findByUserIdOrderByIdDesc(ObjectId userId);

    //TODO сейчас не сработает, т.к. User хоть и не встроен в UserAnswersEntity (@DBRef - true linking), но в User primary key теперь не name, а id
//    List<UserAnswersEntity> findAllByUser_NameOrderByPassDateDesc(String name);

    //должен быть такой же как  findAllByUserIdOrderByPassDateDesc, так как ObjectId  ("OrderById") отсортирован по дате создания
    //правда с точностью 1 сек. Но наверное findAllByUserIdOrderByPassDateDesc будет использовать сортировку
    // в памяти, а  findAllByUser_NameOrderByIdDesc нет
    Optional<List<UserAnswersEntity>> findAllByUser_IdOrderByIdDesc(ObjectId userId);
    Optional<UserAnswersEntity> findTopByUser_IdOrderByPassDateDesc(ObjectId userId);

    List<UserAnswersEntity> findAllByUser_IdAndPassedOrderByPassDateDesc(ObjectId userId, Boolean passed);

    @Query("{$find: {'user.$id': ?0, 'passed': ?1}, $sort: {'passDate': -1}, $limit: 1}")
    UserAnswersEntity findAllByUser_IdAndPassedAndLastPassDate(ObjectId userId, Boolean passed);

    Optional<UserAnswersEntity> findTopByUser_IdAndPassedOrderByPassDateDesc(ObjectId userId, Boolean passed);

//    Optional<UserAnswersEntity> findFirstByUser_NameOrderByIdDesc(String name);
//    Optional<List<UserAnswersEntity>> findAllByUser_NameOrderByIdDesc(String name);

//    void removeAllByUser_Id (ObjectId userId);
    void removeAllByUserId (ObjectId userId);
}
