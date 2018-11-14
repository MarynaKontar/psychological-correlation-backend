package com.psycorp.model.entity;

import com.psycorp.model.enums.Area;
import lombok.Data;

/**
 * Comment for user match
 */
@Data
public class UserMatchComment {

    private Double result;
    private Area aspect;
    private String aspectDescription;
    private String level;
    private String header;
    private String foreword; // предисловие
    private String accent; // то, что будем выделять (bold,...)
    private String mainText;
}
