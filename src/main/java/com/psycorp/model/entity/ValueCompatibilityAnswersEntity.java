package com.psycorp.model.entity;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Набор попарных сравнений по трем полям, по шесть шкал в каждом поле для данного пользователя
 */

@Data
@Document
public class ValueCompatibilityAnswersEntity extends AbstractEntity{
    @Id
    private ObjectId id;
    private ObjectId userId;
    @NotEmpty @Valid
    private List<Choice> userAnswers;
    @CreatedDate
    private LocalDateTime creationDate;
    @LastModifiedDate
    private LocalDateTime passDate;
    private Boolean passed;
}
