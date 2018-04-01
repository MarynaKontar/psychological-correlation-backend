package com.psycorp.model.dto;

import com.psycorp.model.entity.Choice;
import lombok.Data;

import java.util.List;

@Data
public class UserAnswersDto {
    private List<ChoiceDto> goal;
    private List<ChoiceDto> quality;
    private List<ChoiceDto> state;
}
