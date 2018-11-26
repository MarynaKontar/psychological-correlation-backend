package com.psycorp.model.objects;

import com.psycorp.model.enums.AspectLevel;
import lombok.Data;
import org.bson.types.ObjectId;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Comment for user match
 */
@Data
public class UserMatchComment {

//    private Double result;
    private String aspect;
    private String aspectDescription;
    private AspectLevel level;
    private String levelName;
    private String header;
    private String foreword; // предисловие
    private String accent; // то, что будем выделять (bold,...)
    private String mainText;


}
