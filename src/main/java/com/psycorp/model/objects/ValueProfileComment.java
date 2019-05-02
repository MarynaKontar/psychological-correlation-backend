package com.psycorp.model.objects;

import lombok.Data;

import java.util.List;

@Data
public class ValueProfileComment {

    private Double result;
    private String scale;
    private String head;
    private String header;
    private List<String> list;
    private String footer;
}
