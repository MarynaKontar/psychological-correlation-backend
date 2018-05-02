package com.psycorp.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserAnswersDto extends AbstractDto {
    //random высылать перед тестированием
    private List<ChoiceDto> goal;
    private List<ChoiceDto> quality;
    private List<ChoiceDto> state;
    private Boolean passed;
}
