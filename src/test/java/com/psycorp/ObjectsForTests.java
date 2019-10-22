package com.psycorp;

import com.psycorp.model.dto.ValueCompatibilityAnswersDto;
import com.psycorp.model.entity.Choice;
import com.psycorp.model.entity.ValueCompatibilityAnswersEntity;
import com.psycorp.model.enums.Area;
import com.psycorp.model.enums.Scale;
import com.psycorp.сonverter.ValueCompatibilityAnswersDtoConverter;
import org.springframework.core.env.Environment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Utils class for creation of test objects
 */
public class ObjectsForTests {

    public static ValueCompatibilityAnswersEntity getValueCompatibilityEntity() {
        ValueCompatibilityAnswersEntity valueCompatibilityAnswersEntity = new ValueCompatibilityAnswersEntity();
        valueCompatibilityAnswersEntity.setUserAnswers(choiceList());
        valueCompatibilityAnswersEntity.setCreationDate(LocalDateTime.of(2019, 10, 19, 15, 50, 55));
        valueCompatibilityAnswersEntity.setPassed(false);
//        valueCompatibilityAnswersEntity.setUserId(-);
        return valueCompatibilityAnswersEntity;
    }

    public static ValueCompatibilityAnswersDto getValueCompatibilityAnswersDto(Environment env) {
        ValueCompatibilityAnswersDtoConverter valueCompatibilityAnswersDtoConverter = new ValueCompatibilityAnswersDtoConverter(env);
        return valueCompatibilityAnswersDtoConverter.transform(getValueCompatibilityEntity());
    }

    /**
     * Returns list of {@link Choice} with three {@link Area}: GOAL, QUALITY and STATE
     * for six {@link Scale}.
     * All firstScales, secondScales and chosenScales are filled.
     * @return list of {@link Choice}.
     */
    private static List<Choice> choiceList(){

        //GOAL
        List<Choice> choiceGoal = new ArrayList<>();
        choiceGoal.add(getChoice(Area.GOAL, Scale.ONE, Scale.TWO, 0));
        choiceGoal.add(getChoice(Area.GOAL, Scale.ONE, Scale.THREE, 1));
        choiceGoal.add(getChoice(Area.GOAL, Scale.ONE, Scale.FOUR, 1));
        choiceGoal.add(getChoice(Area.GOAL, Scale.ONE, Scale.FIVE, 1));
        choiceGoal.add(getChoice(Area.GOAL, Scale.ONE, Scale.SIX, 0));

        choiceGoal.add(getChoice(Area.GOAL, Scale.TWO, Scale.THREE, 0));
        choiceGoal.add(getChoice(Area.GOAL, Scale.TWO, Scale.FOUR, 1));
        choiceGoal.add(getChoice(Area.GOAL, Scale.TWO, Scale.FIVE, 1));
        choiceGoal.add(getChoice(Area.GOAL, Scale.TWO, Scale.SIX, 0));

        choiceGoal.add(getChoice(Area.GOAL, Scale.THREE, Scale.FOUR, 0));
        choiceGoal.add(getChoice(Area.GOAL, Scale.THREE, Scale.FIVE, 1));
        choiceGoal.add(getChoice(Area.GOAL, Scale.THREE, Scale.SIX, 0));

        choiceGoal.add(getChoice(Area.GOAL, Scale.FOUR, Scale.FIVE, 1));
        choiceGoal.add(getChoice(Area.GOAL, Scale.FOUR, Scale.SIX, 0));

        choiceGoal.add(getChoice(Area.GOAL, Scale.FIVE, Scale.SIX, 0));


        //QUALITY
        List<Choice> choiceQuality = new ArrayList<>();
        choiceQuality.add(getChoice(Area.QUALITY, Scale.ONE, Scale.TWO, 0));
        choiceQuality.add(getChoice(Area.QUALITY, Scale.ONE, Scale.THREE,  1));
        choiceQuality.add(getChoice(Area.QUALITY, Scale.ONE, Scale.FOUR, 1));
        choiceQuality.add(getChoice(Area.QUALITY, Scale.ONE, Scale.FIVE, 0));
        choiceQuality.add(getChoice(Area.QUALITY, Scale.ONE, Scale.SIX, 0));

        choiceQuality.add(getChoice(Area.QUALITY, Scale.TWO, Scale.THREE, 1));
        choiceQuality.add(getChoice(Area.QUALITY, Scale.TWO, Scale.FOUR, 0));
        choiceQuality.add(getChoice(Area.QUALITY, Scale.TWO, Scale.FIVE, 1));
        choiceQuality.add(getChoice(Area.QUALITY, Scale.TWO, Scale.SIX, 1));

        choiceQuality.add(getChoice(Area.QUALITY, Scale.THREE, Scale.FOUR, 0));
        choiceQuality.add(getChoice(Area.QUALITY, Scale.THREE, Scale.FIVE, 0));
        choiceQuality.add(getChoice(Area.QUALITY, Scale.THREE, Scale.SIX, 0));

        choiceQuality.add(getChoice(Area.QUALITY, Scale.FOUR, Scale.FIVE, 1));
        choiceQuality.add(getChoice(Area.QUALITY, Scale.FOUR, Scale.SIX, 1));

        choiceQuality.add(getChoice(Area.QUALITY, Scale.FIVE, Scale.SIX, 0));


        //STATE
        List<Choice> choiceState = new ArrayList<>();
        choiceState.add(getChoice(Area.STATE, Scale.ONE, Scale.TWO, 1));
        choiceState.add(getChoice(Area.STATE, Scale.ONE, Scale.THREE, 1));
        choiceState.add(getChoice(Area.STATE, Scale.ONE, Scale.FOUR, 0));
        choiceState.add(getChoice(Area.STATE, Scale.ONE, Scale.FIVE, 1));
        choiceState.add(getChoice(Area.STATE, Scale.ONE, Scale.SIX, 1));

        choiceState.add(getChoice(Area.STATE, Scale.TWO, Scale.THREE, 0));
        choiceState.add(getChoice(Area.STATE, Scale.TWO, Scale.FOUR, 1));
        choiceState.add(getChoice(Area.STATE, Scale.TWO, Scale.FIVE, 0));
        choiceState.add(getChoice(Area.STATE, Scale.TWO, Scale.SIX, 1));

        choiceState.add(getChoice(Area.STATE, Scale.THREE, Scale.FOUR, 1));
        choiceState.add(getChoice(Area.STATE, Scale.THREE, Scale.FIVE, 1));
        choiceState.add(getChoice(Area.STATE, Scale.THREE, Scale.SIX, 1));

        choiceState.add(getChoice(Area.STATE, Scale.FOUR, Scale.FIVE, 0));
        choiceState.add(getChoice(Area.STATE, Scale.FOUR, Scale.SIX, 1));

        choiceState.add(getChoice(Area.STATE, Scale.FIVE, Scale.SIX, 0));


        List<Choice> choices = new ArrayList<>(choiceGoal);
        choices.addAll(choiceQuality);
        choices.addAll(choiceState);

        return choices;
    }

    /**
     * Gets choice for given area.
     * @param area must not be {@literal null}.
     * @param scaleOne must not be {@literal null}.
     * @param scaleTwo must not be {@literal null}.
     * @return {@link Choice} for given area.
     */
    private static Choice getChoice(Area area, Scale scaleOne, Scale scaleTwo, Integer chosenScaleNumber) {
        if (chosenScaleNumber != 0 && chosenScaleNumber != 1) { throw new IllegalArgumentException("chosenScaleNumber must be 0 or 1, but it is: " + chosenScaleNumber);}
        Choice choice = new Choice();
        choice.setArea(area);

        List<Scale> scales = Arrays.asList(scaleOne, scaleTwo);

        choice.setFirstScale(scales.get(0));
        choice.setSecondScale(scales.get(1));
        choice.setChosenScale(chosenScaleNumber == 0 ? choice.getFirstScale() : choice.getSecondScale());

        return choice;
    }

    public static String getValueCompatibilityAnswersDtoJson() {
        return "{\n" +
                "   \n" +
                "    \"goal\": [\n" +
                "        {\n" +
                "        \t\"question\": \"Что для вас важнее и ценнее? Какая жизненная цель является для вас более значимой и приоритетной?\",\n" +
                "            \"area\": {\n" +
                "                \"area\": \"GOAL\",\n" +
                "                \"areaName\": \"Жизненные цели\"\n" +
                "            },\n" +
                "            \"firstScale\": {\n" +
                "                \"scale\": \"FIVE\",\n" +
                "                \"scaleHeader\": \"ТВОРЧЕСТВО\",\n" +
                "                \"scaleDescription\": \"Созидание чего-то нового. Творческая самореализация, творческое самовыражение в том, что ты делаешь.\"\n" +
                "            },\n" +
                "            \"secondScale\": {\n" +
                "                \"scale\": \"THREE\",\n" +
                "                \"scaleHeader\": \"ДОСТИЖЕНИЯ\",\n" +
                "                \"scaleDescription\": \"Профессиональные, спортивные и личные успехи, достижения и победы.\"\n" +
                "            },\n" +
                "            \"chosenScale\": {\n" +
                "                 \"scale\": \"FIVE\",\n" +
                "                \"scaleHeader\": \"ТВОРЧЕСТВО\",\n" +
                "                \"scaleDescription\": \"Созидание чего-то нового. Творческая самореализация, творческое самовыражение в том, что ты делаешь.\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "        \t\"question\": \"Что для вас важнее и ценнее? Какая жизненная цель является для вас более значимой и приоритетной?\",\n" +
                "            \"area\": {\n" +
                "                \"area\": \"GOAL\",\n" +
                "                \"areaName\": \"Жизненные цели\"\n" +
                "            },\n" +
                "            \"firstScale\": {\n" +
                "                \"scale\": \"ONE\",\n" +
                "                \"scaleHeader\": \"ЗДОРОВЬЕ\",\n" +
                "                \"scaleDescription\": \"Поддержание крепкого физического и душевного здоровья. Обеспечение себя достаточным количеством жизненных сил и энергии.\"\n" +
                "            },\n" +
                "            \"secondScale\": {\n" +
                "                \"scale\": \"THREE\",\n" +
                "                \"scaleHeader\": \"ДОСТИЖЕНИЯ\",\n" +
                "                \"scaleDescription\": \"Профессиональные, спортивные и личные успехи, достижения и победы.\"\n" +
                "            },\n" +
                "            \"chosenScale\": {\n" +
                "                \"scale\": \"ONE\",\n" +
                "                \"scaleHeader\": \"ЗДОРОВЬЕ\",\n" +
                "                \"scaleDescription\": \"Поддержание крепкого физического и душевного здоровья. Обеспечение себя достаточным количеством жизненных сил и энергии.\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "        \t\"question\": \"Что для вас важнее и ценнее? Какая жизненная цель является для вас более значимой и приоритетной?\",\n" +
                "            \"area\": {\n" +
                "                \"area\": \"GOAL\",\n" +
                "                \"areaName\": \"Жизненные цели\"\n" +
                "            },\n" +
                "            \"firstScale\": {\n" +
                "                \"scale\": \"FIVE\",\n" +
                "                \"scaleHeader\": \"ТВОРЧЕСТВО\",\n" +
                "                \"scaleDescription\": \"Созидание чего-то нового. Творческая самореализация, творческое самовыражение в том, что ты делаешь.\"\n" +
                "            },\n" +
                "            \"secondScale\": {\n" +
                "                \"scale\": \"TWO\",\n" +
                "                \"scaleHeader\": \"УДОВОЛЬСТВИЯ\",\n" +
                "                \"scaleDescription\": \"Наслаждение жизнью. Получение удовольствия от всего, что ты делаешь (от еды, секса, работы, общения, развлечений и т.д.).\"\n" +
                "            },\n" +
                "            \"chosenScale\": {\n" +
                "                \"scale\": \"TWO\",\n" +
                "                \"scaleHeader\": \"УДОВОЛЬСТВИЯ\",\n" +
                "                \"scaleDescription\": \"Наслаждение жизнью. Получение удовольствия от всего, что ты делаешь (от еды, секса, работы, общения, развлечений и т.д.).\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "        \t\"question\": \"Что для вас важнее и ценнее? Какая жизненная цель является для вас более значимой и приоритетной?\",\n" +
                "            \"area\": {\n" +
                "                \"area\": \"GOAL\",\n" +
                "                \"areaName\": \"Жизненные цели\"\n" +
                "            },\n" +
                "            \"firstScale\": {\n" +
                "                \"scale\": \"FIVE\",\n" +
                "                \"scaleHeader\": \"ТВОРЧЕСТВО\",\n" +
                "                \"scaleDescription\": \"Созидание чего-то нового. Творческая самореализация, творческое самовыражение в том, что ты делаешь.\"\n" +
                "            },\n" +
                "            \"secondScale\": {\n" +
                "                \"scale\": \"SIX\",\n" +
                "                \"scaleHeader\": \"РАЗВИТИЕ\",\n" +
                "                \"scaleDescription\": \"Личностное, интеллектуальное и физическое развитие. Самоисследование и самосовершенствование. Раскрытие своего внутреннего потенциала.\"\n" +
                "            },\n" +
                "            \"chosenScale\": {\n" +
                "                \"scale\": \"SIX\",\n" +
                "                \"scaleHeader\": \"РАЗВИТИЕ\",\n" +
                "                \"scaleDescription\": \"Личностное, интеллектуальное и физическое развитие. Самоисследование и самосовершенствование. Раскрытие своего внутреннего потенциала.\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "        \t\"question\": \"Что для вас важнее и ценнее? Какая жизненная цель является для вас более значимой и приоритетной?\",\n" +
                "            \"area\": {\n" +
                "                \"area\": \"GOAL\",\n" +
                "                \"areaName\": \"Жизненные цели\"\n" +
                "            },\n" +
                "            \"firstScale\": {\n" +
                "                \"scale\": \"SIX\",\n" +
                "                \"scaleHeader\": \"РАЗВИТИЕ\",\n" +
                "                \"scaleDescription\": \"Личностное, интеллектуальное и физическое развитие. Самоисследование и самосовершенствование. Раскрытие своего внутреннего потенциала.\"\n" +
                "            },\n" +
                "            \"secondScale\": {\n" +
                "                \"scale\": \"TWO\",\n" +
                "                \"scaleHeader\": \"УДОВОЛЬСТВИЯ\",\n" +
                "                \"scaleDescription\": \"Наслаждение жизнью. Получение удовольствия от всего, что ты делаешь (от еды, секса, работы, общения, развлечений и т.д.).\"\n" +
                "            },\n" +
                "            \"chosenScale\": {\n" +
                "                \"scale\": \"SIX\",\n" +
                "                \"scaleHeader\": \"РАЗВИТИЕ\",\n" +
                "                \"scaleDescription\": \"Личностное, интеллектуальное и физическое развитие. Самоисследование и самосовершенствование. Раскрытие своего внутреннего потенциала.\"\n" +
                "           }\n" +
                "        },\n" +
                "        {\n" +
                "        \t\"question\": \"Что для вас важнее и ценнее? Какая жизненная цель является для вас более значимой и приоритетной?\",\n" +
                "            \"area\": {\n" +
                "                \"area\": \"GOAL\",\n" +
                "                \"areaName\": \"Жизненные цели\"\n" +
                "            },\n" +
                "            \"firstScale\": {\n" +
                "                \"scale\": \"THREE\",\n" +
                "                \"scaleHeader\": \"ДОСТИЖЕНИЯ\",\n" +
                "                \"scaleDescription\": \"Профессиональные, спортивные и личные успехи, достижения и победы.\"\n" +
                "            },\n" +
                "            \"secondScale\": {\n" +
                "                \"scale\": \"SIX\",\n" +
                "                \"scaleHeader\": \"РАЗВИТИЕ\",\n" +
                "                \"scaleDescription\": \"Личностное, интеллектуальное и физическое развитие. Самоисследование и самосовершенствование. Раскрытие своего внутреннего потенциала.\"\n" +
                "            },\n" +
                "            \"chosenScale\": {\n" +
                "                \"scale\": \"SIX\",\n" +
                "                \"scaleHeader\": \"РАЗВИТИЕ\",\n" +
                "                \"scaleDescription\": \"Личностное, интеллектуальное и физическое развитие. Самоисследование и самосовершенствование. Раскрытие своего внутреннего потенциала.\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "        \t\"question\": \"Что для вас важнее и ценнее? Какая жизненная цель является для вас более значимой и приоритетной?\",\n" +
                "            \"area\": {\n" +
                "                \"area\": \"GOAL\",\n" +
                "                \"areaName\": \"Жизненные цели\"\n" +
                "            },\n" +
                "            \"firstScale\": {\n" +
                "                \"scale\": \"TWO\",\n" +
                "                \"scaleHeader\": \"УДОВОЛЬСТВИЯ\",\n" +
                "                \"scaleDescription\": \"Наслаждение жизнью. Получение удовольствия от всего, что ты делаешь (от еды, секса, работы, общения, развлечений и т.д.).\"\n" +
                "            },\n" +
                "            \"secondScale\": {\n" +
                "                \"scale\": \"FOUR\",\n" +
                "                \"scaleHeader\": \"ГАРМОНИЧНЫЕ ОТНОШЕНИЯ\",\n" +
                "                \"scaleDescription\": \"Построение и поддержание гармоничных близких, эмоционально тёплых отношений, основанных на взаимной симпатии и душевной близости.\"\n" +
                "            },\n" +
                "            \"chosenScale\": {\n" +
                "                \"scale\": \"TWO\",\n" +
                "                \"scaleHeader\": \"УДОВОЛЬСТВИЯ\",\n" +
                "                \"scaleDescription\": \"Наслаждение жизнью. Получение удовольствия от всего, что ты делаешь (от еды, секса, работы, общения, развлечений и т.д.).\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "        \t\"question\": \"Что для вас важнее и ценнее? Какая жизненная цель является для вас более значимой и приоритетной?\",\n" +
                "            \"area\": {\n" +
                "                \"area\": \"GOAL\",\n" +
                "                \"areaName\": \"Жизненные цели\"\n" +
                "            },\n" +
                "            \"firstScale\": {\n" +
                "                \"scale\": \"FOUR\",\n" +
                "                \"scaleHeader\": \"ГАРМОНИЧНЫЕ ОТНОШЕНИЯ\",\n" +
                "                \"scaleDescription\": \"Построение и поддержание гармоничных близких, эмоционально тёплых отношений, основанных на взаимной симпатии и душевной близости.\"\n" +
                "            },\n" +
                "            \"secondScale\": {\n" +
                "                \"scale\": \"FIVE\",\n" +
                "                \"scaleHeader\": \"ТВОРЧЕСТВО\",\n" +
                "                \"scaleDescription\": \"Созидание чего-то нового. Творческая самореализация, творческое самовыражение в том, что ты делаешь.\"\n" +
                "            },\n" +
                "            \"chosenScale\": {\n" +
                "                \"scale\": \"FIVE\",\n" +
                "                \"scaleHeader\": \"ТВОРЧЕСТВО\",\n" +
                "                \"scaleDescription\": \"Созидание чего-то нового. Творческая самореализация, творческое самовыражение в том, что ты делаешь.\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "        \t\"question\": \"Что для вас важнее и ценнее? Какая жизненная цель является для вас более значимой и приоритетной?\",\n" +
                "            \"area\": {\n" +
                "                \"area\": \"GOAL\",\n" +
                "                \"areaName\": \"Жизненные цели\"\n" +
                "            },\n" +
                "            \"firstScale\": {\n" +
                "                \"scale\": \"TWO\",\n" +
                "                \"scaleHeader\": \"УДОВОЛЬСТВИЯ\",\n" +
                "                \"scaleDescription\": \"Наслаждение жизнью. Получение удовольствия от всего, что ты делаешь (от еды, секса, работы, общения, развлечений и т.д.).\"\n" +
                "            },\n" +
                "            \"secondScale\": {\n" +
                "                \"scale\": \"THREE\",\n" +
                "                \"scaleHeader\": \"ДОСТИЖЕНИЯ\",\n" +
                "                \"scaleDescription\": \"Профессиональные, спортивные и личные успехи, достижения и победы.\"\n" +
                "            },\n" +
                "            \"chosenScale\": {\n" +
                "                \"scale\": \"TWO\",\n" +
                "                \"scaleHeader\": \"УДОВОЛЬСТВИЯ\",\n" +
                "                \"scaleDescription\": \"Наслаждение жизнью. Получение удовольствия от всего, что ты делаешь (от еды, секса, работы, общения, развлечений и т.д.).\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "        \t\"question\": \"Что для вас важнее и ценнее? Какая жизненная цель является для вас более значимой и приоритетной?\",\n" +
                "            \"area\": {\n" +
                "                \"area\": \"GOAL\",\n" +
                "                \"areaName\": \"Жизненные цели\"\n" +
                "            },\n" +
                "            \"firstScale\": {\n" +
                "                \"scale\": \"FIVE\",\n" +
                "                \"scaleHeader\": \"ТВОРЧЕСТВО\",\n" +
                "                \"scaleDescription\": \"Созидание чего-то нового. Творческая самореализация, творческое самовыражение в том, что ты делаешь.\"\n" +
                "            },\n" +
                "            \"secondScale\": {\n" +
                "                \"scale\": \"ONE\",\n" +
                "                \"scaleHeader\": \"ЗДОРОВЬЕ\",\n" +
                "                \"scaleDescription\": \"Поддержание крепкого физического и душевного здоровья. Обеспечение себя достаточным количеством жизненных сил и энергии.\"\n" +
                "            },\n" +
                "            \"chosenScale\": {\n" +
                "                \"scale\": \"ONE\",\n" +
                "                \"scaleHeader\": \"ЗДОРОВЬЕ\",\n" +
                "                \"scaleDescription\": \"Поддержание крепкого физического и душевного здоровья. Обеспечение себя достаточным количеством жизненных сил и энергии.\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "        \t\"question\": \"Что для вас важнее и ценнее? Какая жизненная цель является для вас более значимой и приоритетной?\",\n" +
                "            \"area\": {\n" +
                "                \"area\": \"GOAL\",\n" +
                "                \"areaName\": \"Жизненные цели\"\n" +
                "            },\n" +
                "            \"firstScale\": {\n" +
                "                \"scale\": \"FOUR\",\n" +
                "                \"scaleHeader\": \"ГАРМОНИЧНЫЕ ОТНОШЕНИЯ\",\n" +
                "                \"scaleDescription\": \"Построение и поддержание гармоничных близких, эмоционально тёплых отношений, основанных на взаимной симпатии и душевной близости.\"\n" +
                "            },\n" +
                "            \"secondScale\": {\n" +
                "                \"scale\": \"ONE\",\n" +
                "                \"scaleHeader\": \"ЗДОРОВЬЕ\",\n" +
                "                \"scaleDescription\": \"Поддержание крепкого физического и душевного здоровья. Обеспечение себя достаточным количеством жизненных сил и энергии.\"\n" +
                "            },\n" +
                "            \"chosenScale\": {\n" +
                "                \"scale\": \"FOUR\",\n" +
                "                \"scaleHeader\": \"ГАРМОНИЧНЫЕ ОТНОШЕНИЯ\",\n" +
                "                \"scaleDescription\": \"Построение и поддержание гармоничных близких, эмоционально тёплых отношений, основанных на взаимной симпатии и душевной близости.\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "        \t\"question\": \"Что для вас важнее и ценнее? Какая жизненная цель является для вас более значимой и приоритетной?\",\n" +
                "            \"area\": {\n" +
                "                \"area\": \"GOAL\",\n" +
                "                \"areaName\": \"Жизненные цели\"\n" +
                "            },\n" +
                "            \"firstScale\": {\n" +
                "                \"scale\": \"ONE\",\n" +
                "                \"scaleHeader\": \"ЗДОРОВЬЕ\",\n" +
                "                \"scaleDescription\": \"Поддержание крепкого физического и душевного здоровья. Обеспечение себя достаточным количеством жизненных сил и энергии.\"\n" +
                "            },\n" +
                "            \"secondScale\": {\n" +
                "                \"scale\": \"SIX\",\n" +
                "                \"scaleHeader\": \"РАЗВИТИЕ\",\n" +
                "                \"scaleDescription\": \"Личностное, интеллектуальное и физическое развитие. Самоисследование и самосовершенствование. Раскрытие своего внутреннего потенциала.\"\n" +
                "            },\n" +
                "            \"chosenScale\": {\n" +
                "                \"scale\": \"SIX\",\n" +
                "                \"scaleHeader\": \"РАЗВИТИЕ\",\n" +
                "                \"scaleDescription\": \"Личностное, интеллектуальное и физическое развитие. Самоисследование и самосовершенствование. Раскрытие своего внутреннего потенциала.\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "        \t\"question\": \"Что для вас важнее и ценнее? Какая жизненная цель является для вас более значимой и приоритетной?\",\n" +
                "            \"area\": {\n" +
                "                \"area\": \"GOAL\",\n" +
                "                \"areaName\": \"Жизненные цели\"\n" +
                "            },\n" +
                "            \"firstScale\": {\n" +
                "                \"scale\": \"SIX\",\n" +
                "                \"scaleHeader\": \"РАЗВИТИЕ\",\n" +
                "                \"scaleDescription\": \"Личностное, интеллектуальное и физическое развитие. Самоисследование и самосовершенствование. Раскрытие своего внутреннего потенциала.\"\n" +
                "            },\n" +
                "            \"secondScale\": {\n" +
                "                \"scale\": \"FOUR\",\n" +
                "                \"scaleHeader\": \"ГАРМОНИЧНЫЕ ОТНОШЕНИЯ\",\n" +
                "                \"scaleDescription\": \"Построение и поддержание гармоничных близких, эмоционально тёплых отношений, основанных на взаимной симпатии и душевной близости.\"\n" +
                "            },\n" +
                "            \"chosenScale\": {\n" +
                "                \"scale\": \"FOUR\",\n" +
                "                \"scaleHeader\": \"ГАРМОНИЧНЫЕ ОТНОШЕНИЯ\",\n" +
                "                \"scaleDescription\": \"Построение и поддержание гармоничных близких, эмоционально тёплых отношений, основанных на взаимной симпатии и душевной близости.\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "        \t\"question\": \"Что для вас важнее и ценнее? Какая жизненная цель является для вас более значимой и приоритетной?\",\n" +
                "            \"area\": {\n" +
                "                \"area\": \"GOAL\",\n" +
                "                \"areaName\": \"Жизненные цели\"\n" +
                "            },\n" +
                "            \"firstScale\": {\n" +
                "                \"scale\": \"TWO\",\n" +
                "                \"scaleHeader\": \"УДОВОЛЬСТВИЯ\",\n" +
                "                \"scaleDescription\": \"Наслаждение жизнью. Получение удовольствия от всего, что ты делаешь (от еды, секса, работы, общения, развлечений и т.д.).\"\n" +
                "            },\n" +
                "            \"secondScale\": {\n" +
                "                \"scale\": \"ONE\",\n" +
                "                \"scaleHeader\": \"ЗДОРОВЬЕ\",\n" +
                "                \"scaleDescription\": \"Поддержание крепкого физического и душевного здоровья. Обеспечение себя достаточным количеством жизненных сил и энергии.\"\n" +
                "            },\n" +
                "            \"chosenScale\": {\n" +
                "                \"scale\": \"TWO\",\n" +
                "                \"scaleHeader\": \"УДОВОЛЬСТВИЯ\",\n" +
                "                \"scaleDescription\": \"Наслаждение жизнью. Получение удовольствия от всего, что ты делаешь (от еды, секса, работы, общения, развлечений и т.д.).\"\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "        \t\"question\": \"Что для вас важнее и ценнее? Какая жизненная цель является для вас более значимой и приоритетной?\",\n" +
                "            \"area\": {\n" +
                "                \"area\": \"GOAL\",\n" +
                "                \"areaName\": \"Жизненные цели\"\n" +
                "            },\n" +
                "            \"firstScale\": {\n" +
                "                \"scale\": \"FOUR\",\n" +
                "                \"scaleHeader\": \"ГАРМОНИЧНЫЕ ОТНОШЕНИЯ\",\n" +
                "                \"scaleDescription\": \"Построение и поддержание гармоничных близких, эмоционально тёплых отношений, основанных на взаимной симпатии и душевной близости.\"\n" +
                "            },\n" +
                "            \"secondScale\": {\n" +
                "                \"scale\": \"THREE\",\n" +
                "                \"scaleHeader\": \"ДОСТИЖЕНИЯ\",\n" +
                "                \"scaleDescription\": \"Профессиональные, спортивные и личные успехи, достижения и победы.\"\n" +
                "            },\n" +
                "            \"chosenScale\": {\n" +
                "                \"scale\": \"FOUR\",\n" +
                "                \"scaleHeader\": \"ГАРМОНИЧНЫЕ ОТНОШЕНИЯ\",\n" +
                "                \"scaleDescription\": \"Построение и поддержание гармоничных близких, эмоционально тёплых отношений, основанных на взаимной симпатии и душевной близости.\"\n" +
                "            }\n" +
                "        }\n" +
                "    ],\n" +
                "    \"quality\": [],\n" +
                "    \"state\": []\n" +
                "}";

    }
}
