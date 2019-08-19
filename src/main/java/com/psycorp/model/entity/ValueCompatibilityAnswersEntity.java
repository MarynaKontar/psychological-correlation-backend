package com.psycorp.model.entity;

import com.psycorp.model.enums.Area;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
 * Entity data level for saving data in database.
 * Stores results of value compatibility test for
 * given user with userId in database.
 * @author Maryna Kontar
 * @author Vitaliy Proskura
 */
@Data
@EqualsAndHashCode(callSuper = true)
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
