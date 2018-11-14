package com.psycorp.model.entity;

import lombok.Data;

import java.util.List;

@Data
public class ValueProfileComment {

    private Double result;
    private String head;
    private String header;
    private List<String> list;
    private String footer;
}
