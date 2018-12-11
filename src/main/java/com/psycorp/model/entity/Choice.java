package com.psycorp.model.entity;

import com.psycorp.model.enums.Area;
import com.psycorp.model.enums.Scale;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Сравнение пары шкал в одном из трех полей. Если chosenScale=null тест еще не пройден
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Choice extends AbstractEntity {
    //TODO такая архитектура подходит для попарного сравнения. Для других видов тестирования (Психологическая совместимость,
    // Сексуальная совместимость, Семейная совместимость (ролевых ожиданий),
    // Совместимость руководитель-подчиненный, Групповая совместимость.)
    //может не подойти

    @NotEmpty @NotNull
    private Area area;
    @NotEmpty
    private Scale firstScale;
    @NotEmpty
    private Scale secondScale;

    private Scale chosenScale;

    public Choice(@NotEmpty Scale firstScale, @NotEmpty Scale secondScale, Scale chosenScale) {
        this.firstScale = firstScale;
        this.secondScale = secondScale;
        this.chosenScale = chosenScale;
    }
}
