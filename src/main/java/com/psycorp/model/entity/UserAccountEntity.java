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
    private List<ObjectId> usersWhoInvitedYouId;
    AccountType accountType;
}
