package com.psycorp.сonverter;

import com.psycorp.exception.BadRequestException;
import com.psycorp.model.dto.ChoiceDto;
import com.psycorp.model.dto.UserAnswersDto;
import com.psycorp.model.entity.Choice;
import com.psycorp.model.entity.UserAnswers;
import com.psycorp.model.enums.Area;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserAnswersDtoConverter extends AbstractDtoConverter<UserAnswers, UserAnswersDto>{
    @Override
    protected UserAnswersDto createNewDto() {
        return new UserAnswersDto();
    }

    @Override
    protected UserAnswers createNewEntity() {
        return new UserAnswers();
    }

    @Override
    protected void convertFromEntity(UserAnswers entity, UserAnswersDto dto) {

        ChoiceDtoConverter choiceDtoConverter = new ChoiceDtoConverter();

        Set<Choice> choices = entity.getUserAnswers();

        Set<Choice> goal =  new HashSet<>(choices);
        goal.removeIf(choice -> choice.getArea()!= Area.GOAL);
        List<ChoiceDto> goalDto = choiceDtoConverter.transform(goal);

        Set<Choice> quality =  new HashSet<>(choices);
        quality.removeIf(choice -> choice.getArea()!= Area.QUALITY);
        List<ChoiceDto> qualityDto = choiceDtoConverter.transform(quality);

        Set<Choice> state =  new HashSet<>(choices);
        state.removeIf(choice -> choice.getArea()!= Area.STATE);
        List<ChoiceDto> stateDto = choiceDtoConverter.transform(state);

        dto.setGoal(goalDto);
        dto.setQuality(qualityDto);
        dto.setState(stateDto);
        dto.setPassed(false);
    }

    @Override
    protected void convertFromDto(UserAnswersDto dto, UserAnswers entity) {
        throw  new BadRequestException("!!!!!НЕ НАПИСАН МЕТОД convertFromDto для UserAnswers");
    }
}
