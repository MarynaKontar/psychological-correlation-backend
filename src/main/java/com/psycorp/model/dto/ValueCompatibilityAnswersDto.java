package com.psycorp.model.dto;

import com.psycorp.model.entity.ValueCompatibilityAnswersEntity;
import lombok.Data;
import org.bson.types.ObjectId;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO (Data Transfer Object) data level
 * for hiding implementation details of {@link ValueCompatibilityAnswersEntity}.
 * @author Maryna Kontar
 */
@Data
public class ValueCompatibilityAnswersDto extends AbstractDto {

    private ObjectId id;
    private ObjectId userId;
    @Valid @Size(min = 15, max = 15)
    private List<ChoiceDto> goal;
    @Valid @Size(min = 0, max = 15)
    private List<ChoiceDto> quality;
    @Valid @Size(min = 0, max = 15)
    private List<ChoiceDto> state;
    private LocalDateTime passDate;
    private Boolean passed;
}
