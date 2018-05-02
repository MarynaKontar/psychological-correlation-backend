package com.psycorp.model.entity;

import lombok.Data;

/** и
 * Расчитанные результаты совместимости двух юзеров
 */
@Data
public class UserMatch extends AbstractEntity  {
    private User userOne;
    private User userTwo;
    private String totalMatch;
    private String goalMatch;
    private String qualityMatch;
    private String stateMatch;


}
