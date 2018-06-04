package com.psycorp.model.dto;

import lombok.Data;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserAnswersDto extends AbstractDto {

    private ObjectId id;
    private String userName;
    private List<ChoiceDto> goal;
    private List<ChoiceDto> quality;
    private List<ChoiceDto> state;
    private LocalDateTime passDate;
    private Boolean passed;
}
