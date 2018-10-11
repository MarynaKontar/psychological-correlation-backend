package com.psycorp.repository;

import com.psycorp.model.entity.UserAnswers;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RepositoryRestResource(collectionResourceRel = "userAnswers", path = "userAnswers")
public interface UserAnswersRepository extends MongoRepository<UserAnswers, ObjectId> {

//    Optional<UserAnswers> findByUserIdOrderByIdDesc(ObjectId userId);

    //TODO сейчас не сработает, т.к. User хоть и не встроен в UserAnswers (@DBRef - true linking), но в User primary key теперь не name, а id
//    List<UserAnswers> findAllByUser_NameOrderByPassDateDesc(String name);

    //должен быть такой же как  findAllByUserIdOrderByPassDateDesc, так как ObjectId  ("OrderById") отсортирован по дате создания
    //правда с точностью 1 сек. Но наверное findAllByUserIdOrderByPassDateDesc будет использовать сортировку
    // в памяти, а  findAllByUser_NameOrderByIdDesc нет
    Optional<List<UserAnswers>> findAllByUser_IdOrderByIdDesc(ObjectId userId);
    Optional<UserAnswers> findTopByUser_IdOrderByPassDateDesc(ObjectId userId);

    List<UserAnswers> findAllByUser_IdAndPassedOrderByPassDateDesc(ObjectId userId, Boolean passed);

    @Query("{$find: {'user.$id': ?0, 'passed': ?1}, $sort: {'passDate': -1}, $limit: 1}")
   UserAnswers findAllByUser_IdAndPassedAndLastPassDate(ObjectId userId, Boolean passed);

    Optional<UserAnswers> findTopByUser_IdAndPassedOrderByPassDateDesc(ObjectId userId, Boolean passed);

//    Optional<UserAnswers> findFirstByUser_NameOrderByIdDesc(String name);
//    Optional<List<UserAnswers>> findAllByUser_NameOrderByIdDesc(String name);

//    void removeAllByUser_Id (ObjectId userId);
    void removeAllByUserId (ObjectId userId);
}
