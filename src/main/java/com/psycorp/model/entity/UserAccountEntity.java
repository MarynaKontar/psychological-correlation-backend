package com.psycorp.model.entity;

import com.psycorp.model.enums.AccountType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@Document(collection = "userAccountEntity")
public class UserAccountEntity extends AbstractEntity {
    @Id
    private ObjectId id;
    private ObjectId userId;
    private AccountType accountType;
    private List<ObjectId> usersWhoInvitedYouId; // пользователи, которые пригласили тебя сравнить профили
    private List<ObjectId> usersWhoYouInviteId; // пользователи, которых ты пригласил сравнить профили
}
