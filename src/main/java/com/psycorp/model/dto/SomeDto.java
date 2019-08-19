package com.psycorp.model.dto;

import com.psycorp.model.entity.Choice;
import com.psycorp.model.entity.User;
import lombok.Data;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO (Data Transfer Object) data level
 * !!!!!! DELETE It was need for test mongoDb lookUp functionality
 * @author Maryna Kontar
 */
@Data
public class SomeDto {
    private ObjectId id;
    private List<Choice> userAnswers;
    private LocalDateTime creationDate;
    private LocalDateTime passDate;
    private Boolean passed;

    private User userInfo;

}
