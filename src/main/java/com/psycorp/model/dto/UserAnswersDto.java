package com.psycorp.model.dto;

import lombok.Data;
import org.bson.types.ObjectId;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserAnswersDto extends AbstractDto {

    private ObjectId id;
//    private String userName;
    private ObjectId userId;
//    @NotEmpty
    @Valid @Size(min = 15, max = 15)
    private List<ChoiceDto> goal;
    @Valid @Size(min = 0, max = 15)
    private List<ChoiceDto> quality;
    @Valid @Size(min = 0, max = 15)
    private List<ChoiceDto> state;
    private LocalDateTime passDate;
    private Boolean passed;
}
