package com.psycorp.model.entity;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Набор попарных сравнений по трем полям, по шесть шкал в каждом поле для каждого юзера
 */

@Data
public class UserAnswers extends AbstractEntity{
    @Id
    private ObjectId id;
    @DBRef
    private User user;
    @NotEmpty @Valid
    private List<Choice> userAnswers;
    @CreatedDate
    private LocalDateTime creationDate;
    @LastModifiedDate
    private LocalDateTime passDate;
    private Boolean passed;
}
