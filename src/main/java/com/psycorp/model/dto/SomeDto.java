package com.psycorp.model.dto;

import com.psycorp.model.entity.Choice;
import com.psycorp.model.entity.User;
import lombok.Data;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SomeDto {
    private ObjectId id;
    private List<Choice> userAnswers;
    private LocalDateTime creationDate;
    private LocalDateTime passDate;
    private Boolean passed;

    private User userInfo;

}
