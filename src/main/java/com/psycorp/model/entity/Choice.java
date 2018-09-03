package com.psycorp.model.entity;

import com.psycorp.model.enums.Area;
import com.psycorp.model.enums.Scale;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * Сравнение пары шкал в одном из трех полей. Если chosenScale=null тест еще не пройден
 */

@Data
public class Choice extends AbstractEntity {
    //TODO такая архитектура подходит для попарного сравнения. Для других видов тестирования (Психологическая совместимость,
    // Сексуальная совместимость, Семейная совместимость (ролевых ожиданий),
    // Совместимость руководитель-подчиненный, Групповая совместимость.)
    //может не подойти

    @NotEmpty
    private Area area;
    @NotEmpty
    private Scale firstScale;
    @NotEmpty
    private Scale secondScale;

    private Scale chosenScale;
}
