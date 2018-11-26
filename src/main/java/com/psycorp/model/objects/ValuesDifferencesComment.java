package com.psycorp.model.objects;

import com.psycorp.model.enums.ScaleLevel;
import lombok.Data;

import java.util.List;

@Data
public class ValuesDifferencesComment {

    private String scale;
    private Integer result;
    private ScaleLevel level;
    private String levelName;
    private List<String> text;
}
