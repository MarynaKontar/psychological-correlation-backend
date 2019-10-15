package com.psycorp;

import com.mongodb.MongoClientURI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.test.context.ActiveProfiles;

//@Configuration
//@ActiveProfiles("test")
//@PropertySource("classpath:application-test.yml")
public class MongoTestConfiguration {
//    @Autowired
//    private Environment env;
//
//    @Bean
//    public MongoDbFactory mongoDbFactory() {
//        String prop = "mongodb://" + env.getProperty("spring.data.mongodb.username") + ":" + env.getProperty("spring.data.mongodb.password") + "@" + env.getProperty("spring.data.mongodb.host") + ":" + env.getProperty("spring.data.mongodb.port") + "/" + env.getProperty("spring.data.mongodb.database");
//        String property = env.getProperty(prop);
//        MongoDbFactory mongoDbFactory = new SimpleMongoDbFactory(new MongoClientURI(prop));
//        return mongoDbFactory;
//    }
//
//    @Bean
//    public MongoTemplate mongoTemplate() {
//        MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory());
//        return mongoTemplate;
//    }
}
