package com.psycorp.model.entity;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Набор попарных сравнений по трем полям, по шесть шкал в каждом поле для каждого юзера
 */

@Data
public class UserAnswers extends AbstractEntity{
    @Id
    private ObjectId id;
    @DBRef
    private User user;
    private Set<Choice> userAnswers;
    @CreatedDate
    private LocalDateTime creationDate;//можно убрать так как дата содержится в ObjectId
    @LastModifiedDate
    private LocalDateTime passDate;
}
