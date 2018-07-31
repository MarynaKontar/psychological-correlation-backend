package com.psycorp.сonverter;

import com.psycorp.exception.BadRequestException;
import com.psycorp.model.dto.ChoiceDto;
import com.psycorp.model.dto.UserAnswersDto;
import com.psycorp.model.entity.Choice;
import com.psycorp.model.entity.UserAnswers;
import com.psycorp.model.enums.Area;
import com.psycorp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
//@PropertySource(value = {"classpath:scales/scalesukrainian.properties"}, encoding = "utf-8", ignoreResourceNotFound = true)
//@PropertySource(value = {"classpath:scales/scalesrussian.properties"}, encoding = "utf-8")
//@PropertySource(value = {"classpath:scales/scalesenglish.properties"}, encoding = "utf-8", ignoreResourceNotFound = true)
//@PropertySource("classpath:errormessages.properties")
public class UserAnswersDtoConverter extends AbstractDtoConverter<UserAnswers, UserAnswersDto>{

    private final UserRepository userRepository;
    private final Environment env;

    @Autowired
    public UserAnswersDtoConverter(UserRepository userRepository, Environment env) {
        this.userRepository = userRepository;
        this.env = env;
    }

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
        if(entity == null || dto == null) throw new BadRequestException(env.getProperty("error.UserAnswersCan`tBeNull"));

        ChoiceDtoConverter choiceDtoConverter = new ChoiceDtoConverter(env);

        List<Choice> choices = entity.getUserAnswers();

        List<Choice> goal =  new ArrayList<>(choices);
//        goal.stream().filter(choice -> choice.getArea() == Area.GOAL);
        goal.removeIf(choice -> choice.getArea()!= Area.GOAL);
        List<ChoiceDto> goalDto = choiceDtoConverter.transform(goal);

        List<Choice> quality  =  new ArrayList<>(choices);
//        quality.stream().filter(choice -> choice.getArea() == Area.QUALITY);
        quality.removeIf(choice -> choice.getArea()!= Area.QUALITY);
        List<ChoiceDto> qualityDto = choiceDtoConverter.transform(quality);

        List<Choice> state  =  new ArrayList<>(choices);
//        state.stream().filter(choice -> choice.getArea() == Area.STATE);
        state.removeIf(choice -> choice.getArea()!= Area.STATE);
        List<ChoiceDto> stateDto = choiceDtoConverter.transform(state);

        dto.setGoal(goalDto);
        dto.setQuality(qualityDto);
        dto.setState(stateDto);
        dto.setPassed(true);
        if(entity.getUser() != null){
        dto.setUserName(entity.getUser().getName());
        }
        dto.setPassDate(entity.getPassDate());
        if(entity.getId() != null) {
            dto.setId(entity.getId());
        }
    }

    @Override
    protected void convertFromDto(UserAnswersDto dto, UserAnswers entity) {
        if(entity == null || dto == null) throw new BadRequestException(env.getProperty("error.UserAnswersCan`tBeNull"));
        ChoiceDtoConverter choiceDtoConverter = new ChoiceDtoConverter(env);
        List<Choice> choices = new ArrayList<>(choiceDtoConverter.transform(dto.getGoal()));
        choices.addAll(choiceDtoConverter.transform(dto.getQuality()));
        choices.addAll(choiceDtoConverter.transform(dto.getState()));

        entity.setUserAnswers(choices);
        entity.setUser(userRepository.findFirstByName(dto.getUserName()));
        entity.setId(dto.getId());
    }
}
