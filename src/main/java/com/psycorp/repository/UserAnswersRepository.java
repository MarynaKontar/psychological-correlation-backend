package com.psycorp.repository;

import com.psycorp.model.entity.UserAnswers;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RepositoryRestResource(collectionResourceRel = "userAnswers", path = "userAnswers")
public interface UserAnswersRepository extends MongoRepository<UserAnswers, ObjectId> {

    //TODO сработает, т.к. User хоть и не встроен в UserAnswers (@DBRef - true linking), но в User primary key теперь name, а не id
//    List<UserAnswers> findAllByUser_NameOrderByPassDateDesc(String name);

    //должен быть такой же как  findAllByUser_NameOrderByPassDateDesc, так как ObjectId  ("OrderById") отсортирован по дате создания
    //правда с точностью 1 сек. Но наверное findAllByUser_NameOrderByPassDateDesc будет использовать сортировку
    // в памяти, а  findAllByUser_NameOrderByIdDesc нет
    List<UserAnswers> findAllByUser_NameOrderByIdDesc(String name);

    UserAnswers findFirstByUser_NameOrderByIdDesc(String name);

    void removeAllByUser_Id (ObjectId userId);
}
