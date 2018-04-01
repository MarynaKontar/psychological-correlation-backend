package com.psycorp.model.entity;

import com.psycorp.model.enums.Area;
import com.psycorp.model.enums.Scale;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Сравнение пары шкал в одном из трех полей. Если chosenScale=null тест еще не пройден
 */

@Data
public class Choice {
    @NotNull
    private Area area;
    @NotNull
    private Scale firstScale;
    @NotNull
    private Scale secondScale;

    private Scale chosenScale;
}
