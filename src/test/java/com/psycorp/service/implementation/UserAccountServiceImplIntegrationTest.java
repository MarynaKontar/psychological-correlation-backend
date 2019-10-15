package com.psycorp.service.implementation;

import com.mongodb.client.MongoCollection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.bson.Document.parse;

@SpringBootTest
@ActiveProfiles("test")
public class UserAccountServiceImplIntegrationTest {
    @Autowired
    private MongoTemplate mongoTemplate;

    @AfterEach
    void tearsDown(){
//        mongoTemplate.dropCollection(User.class);
//        mongoTemplate.dropCollection(UserAccountEntity.class);
//        mongoTemplate.dropCollection(CredentialsEntity.class);
//        mongoTemplate.dropCollection(TokenEntity.class);
//        mongoTemplate.dropCollection(ValueCompatibilityAnswersEntity.class);
//        mongoTemplate.dropCollection(UserMatchEntity.class);
    }
    @Test
    void getAllUserForMatchingPassedTest() {

//        MongoCollection mongoCollection = mongoTemplate.createCollection("testyu");
        MongoCollection userCollection = mongoTemplate.getCollection("user");
        userCollection.insertOne(parse("{\"name\":\"ddffddffddffggg\"}"));
        System.out.println("!!!!!!!!!!!!=======" + userCollection.find().first().toString());
//        MongoDatabase mongoDatabase = mongoClient.getDatabase("test");
//        mongoDatabase.createCollection("testCollection");
//        MongoCollection<Document> collection = mongoDatabase.getCollection("testCollection");
//        collection.insertOne(new Document(parse("{\"fg\":\"yyy\", \"ghhjj\":\"yyy\"}")));
//        System.out.println(collection.find().first().toJson());

    }

}
