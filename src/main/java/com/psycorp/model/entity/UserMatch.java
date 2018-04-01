package com.psycorp.model.entity;

import javax.persistence.Entity;

/**
 * Расчитанные результаты совместимости двух юзеров
 */

@Entity
public class UserMatch extends AbstractEntity {
    private User userOne;
    private User userTwo;
    private String totalMatch;
    private String goalMatch;
    private String qualityMatch;
    private String stateMatch;
}
