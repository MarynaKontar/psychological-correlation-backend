package com.psycorp.service.implementation;

import com.psycorp.model.dto.AreaDto;
import com.psycorp.model.dto.ChoiceDto;
import com.psycorp.model.dto.ScaleDto;
import com.psycorp.model.entity.UserAnswers;
import com.psycorp.model.enums.Area;
import com.psycorp.model.enums.Scale;
import com.psycorp.repository.UserAnswersRepository;
import com.psycorp.service.UserAnswersService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@PropertySource(value = {"classpath:scales/scalesrussian.properties"}, encoding = "utf-8")
@PropertySource(value = {"classpath:scales/scalesukrainian.properties"}, encoding = "utf-8", ignoreResourceNotFound = true)
public class UserAnswersServiceImpl implements UserAnswersService {

    @Autowired
    private UserAnswersRepository userAnswersRepository;

    @Autowired
    private Environment env;

    //TODO добавить проверку всех значений и соответствуюшии им Exceptions
    @Override
    public UserAnswers insert(UserAnswers userAnswers){
        return userAnswersRepository.insert(userAnswers);
    }

    @Override
    public Set<UserAnswers> findAllUserAnswersByUser_IdOrderByPassDateDesc(ObjectId userId) {
        return userAnswersRepository.findAllByUser_IdOrderByPassDateDesc(userId);
    }

    @Override
    public List<ChoiceDto> choiceDtoList(){

        //GOAL
        List<ChoiceDto> choiceDtoGoal = new ArrayList<>();
        choiceDtoGoal.add(getChoiceDto("goal", "health","pleasure"));
        choiceDtoGoal.add(getChoiceDto("goal", "health","achievements"));
        choiceDtoGoal.add(getChoiceDto("goal", "health","harmoniousrelationship"));
        choiceDtoGoal.add(getChoiceDto("goal", "health","creativity"));
        choiceDtoGoal.add(getChoiceDto("goal", "health","development"));

        choiceDtoGoal.add(getChoiceDto("goal", "pleasure","achievements"));
        choiceDtoGoal.add(getChoiceDto("goal", "pleasure","harmoniousrelationship"));
        choiceDtoGoal.add(getChoiceDto("goal", "pleasure","creativity"));
        choiceDtoGoal.add(getChoiceDto("goal", "pleasure","development"));

        choiceDtoGoal.add(getChoiceDto("goal", "achievements","harmoniousrelationship"));
        choiceDtoGoal.add(getChoiceDto("goal", "achievements","creativity"));
        choiceDtoGoal.add(getChoiceDto("goal", "achievements","development"));

        choiceDtoGoal.add(getChoiceDto("goal", "harmoniousrelationship","creativity"));
        choiceDtoGoal.add(getChoiceDto("goal", "harmoniousrelationship","development"));

        choiceDtoGoal.add(getChoiceDto("goal", "creativity","development"));

        Collections.shuffle(choiceDtoGoal);


        //QUALITY
        List<ChoiceDto> choiceDtoQuality = new ArrayList<>();
        choiceDtoQuality.add(getChoiceDto("quality", "durability","hedonism"));
        choiceDtoQuality.add(getChoiceDto("quality", "durability","willpower"));
        choiceDtoQuality.add(getChoiceDto("quality", "durability","goodwill"));
        choiceDtoQuality.add(getChoiceDto("quality", "durability","creativity"));
        choiceDtoQuality.add(getChoiceDto("quality", "durability","wisdom"));

        choiceDtoQuality.add(getChoiceDto("quality", "hedonism","willpower"));
        choiceDtoQuality.add(getChoiceDto("quality", "hedonism","goodwill"));
        choiceDtoQuality.add(getChoiceDto("quality", "hedonism","creativity"));
        choiceDtoQuality.add(getChoiceDto("quality", "hedonism","wisdom"));

        choiceDtoQuality.add(getChoiceDto("quality", "willpower","goodwill"));
        choiceDtoQuality.add(getChoiceDto("quality", "willpower","creativity"));
        choiceDtoQuality.add(getChoiceDto("quality", "willpower","wisdom"));

        choiceDtoQuality.add(getChoiceDto("quality", "goodwill","creativity"));
        choiceDtoQuality.add(getChoiceDto("quality", "goodwill","wisdom"));

        choiceDtoQuality.add(getChoiceDto("quality", "creativity","wisdom"));

        Collections.shuffle(choiceDtoQuality);

        //STATE
        List<ChoiceDto> choiceDtoState = new ArrayList<>();
        choiceDtoState.add(getChoiceDto("state", "sense_of_security","comfort"));
        choiceDtoState.add(getChoiceDto("state", "sense_of_security","self_confidence"));
        choiceDtoState.add(getChoiceDto("state", "sense_of_security","love"));
        choiceDtoState.add(getChoiceDto("state", "sense_of_security","enthusiasm"));
        choiceDtoState.add(getChoiceDto("state", "sense_of_security","clarity"));

        choiceDtoState.add(getChoiceDto("state", "comfort","self_confidence"));
        choiceDtoState.add(getChoiceDto("state", "comfort","love"));
        choiceDtoState.add(getChoiceDto("state", "comfort","enthusiasm"));
        choiceDtoState.add(getChoiceDto("state", "comfort","clarity"));

        choiceDtoState.add(getChoiceDto("state", "self_confidence","love"));
        choiceDtoState.add(getChoiceDto("state", "self_confidence","enthusiasm"));
        choiceDtoState.add(getChoiceDto("state", "self_confidence","clarity"));

        choiceDtoState.add(getChoiceDto("state", "love","enthusiasm"));
        choiceDtoState.add(getChoiceDto("state", "love","clarity"));

        choiceDtoState.add(getChoiceDto("state", "enthusiasm","clarity"));

        Collections.shuffle(choiceDtoState);


        List<ChoiceDto> choiceDto = new ArrayList<>(choiceDtoGoal);
        choiceDto.addAll(choiceDtoQuality);
        choiceDto.addAll(choiceDtoState);

        return choiceDto;
    }


    private ChoiceDto getChoiceDto(String area, String scaleNameOne, String scaleNameTwo) {

        String scaleOne = area + ".scale_" + scaleNameOne;//"goal.scale_health"
        String scaleTwo = area + ".scale_" + scaleNameTwo;//"goal.scale_pleasure"

        String scaleTextOne = area + "." + scaleNameOne;//"goal.health"
        String scaleTextTwo = area + "." + scaleNameTwo;//"goal.pleasure"

        area = area + ".area";//"goal.area"
        String areaName = area + ".name";//"goal.area.name"

        //TODO  или сделать метод createNewDto() public или просто писать new ChoiceDto()
        ChoiceDto choiceDto1 = new ChoiceDto();

        AreaDto areaDto = new AreaDto();
        areaDto.setArea(Area.valueOf(env.getProperty(area)));
        areaDto.setAreaName(env.getProperty(areaName));

        choiceDto1.setArea(areaDto);


        ScaleDto scaleDto1 = new ScaleDto();
        scaleDto1.setScaleName(env.getProperty(scaleTextOne));
        scaleDto1.setScale(Scale.valueOf(env.getProperty(scaleOne)));

        choiceDto1.setFirstScale(scaleDto1);

        ScaleDto scaleDto2 = new ScaleDto();
        scaleDto2.setScaleName(env.getProperty(scaleTextTwo));
        scaleDto2.setScale(Scale.valueOf(env.getProperty(scaleTwo)));

        choiceDto1.setSecondScale(scaleDto2);
        return choiceDto1;
    }
}
