package com.psycorp.сonverter;

import com.psycorp.model.dto.ChoiceDto;
import com.psycorp.model.dto.UserAnswersDto;
import com.psycorp.model.dto.ValueProfileDto;
import com.psycorp.model.dto.ValueProfileElementDto;
import com.psycorp.model.entity.Choice;
import com.psycorp.model.entity.UserAnswers;
import com.psycorp.model.entity.ValueProfile;
import com.psycorp.model.enums.Area;
import com.psycorp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
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

        ChoiceDtoConverter choiceDtoConverter = new ChoiceDtoConverter(env);

        List<Choice> choices = entity.getUserAnswers();

        List<Choice> goal =  new ArrayList<>(choices);
//        goal.stream().filter(choice -> choice.getArea() == Area.GOAL);
        goal.removeIf(choice -> choice.getArea()!= Area.GOAL);
        List<ChoiceDto> goalDto = choiceDtoConverter.transform(goal);

        List<Choice> quality  =  new ArrayList<>(choices);
        quality.removeIf(choice -> choice.getArea()!= Area.QUALITY);
        List<ChoiceDto> qualityDto = choiceDtoConverter.transform(quality);

        List<Choice> state  =  new ArrayList<>(choices);
        state.removeIf(choice -> choice.getArea()!= Area.STATE);
        List<ChoiceDto> stateDto = choiceDtoConverter.transform(state);

        dto.setGoal(goalDto);
        dto.setQuality(qualityDto);
        dto.setState(stateDto);
        if(entity.getUser() != null){
//            dto.setUserName(entity.getUser().getName());
            dto.setUserId(entity.getUser().getId());
        }
        dto.setPassDate(entity.getPassDate());
        if(entity.getId() != null) {
            dto.setId(entity.getId());
        }
        dto.setPassed(entity.getPassed());
    }

    @Override
    protected void convertFromDto(UserAnswersDto dto, UserAnswers entity) {

        ChoiceDtoConverter choiceDtoConverter = new ChoiceDtoConverter(env);
        List<Choice> choices = new ArrayList<>(choiceDtoConverter.transform(dto.getGoal()));
        choices.addAll(choiceDtoConverter.transform(dto.getQuality()));
        choices.addAll(choiceDtoConverter.transform(dto.getState()));

        entity.setUserAnswers(choices);

//        Boolean present1 = userRepository.existsById(dto.getUserId());
//        Boolean present2 = userRepository.findById(dto.getUserId()).isPresent();
//        Optional<User> user = userRepository.findById(dto.getUserId());
        if(dto.getId() != null && userRepository.existsById(dto.getUserId())) {
            entity.setUser(userRepository.findById(dto.getUserId()).get());
        }
        entity.setId(dto.getId());
    }

    public ValueProfileDto convertToValueProfileDtoList(ValueProfile valueProfile) {
        List<ValueProfileElementDto> valueProfileElementDtos = new ArrayList<>();

        valueProfile.getScaleResult().forEach((scale, comment) ->
                {
//                    comment.setResult(comment.getResult() * 100);
                    valueProfileElementDtos.add(
                            new ValueProfileElementDto(env.getProperty(scale.name()), comment.getResult(), comment)
                    );
                }
        );

        ValueProfileDto valueProfileDto = new ValueProfileDto();
        valueProfileDto.setValueProfileElements(valueProfileElementDtos);
        valueProfileDto.setIsPrincipalUser(valueProfile.getIsPrincipalUser());
        return valueProfileDto;
    }
}
