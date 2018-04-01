package com.psycorp.model.dto;

import lombok.Data;

@Data
public class UserMatchDto extends AbstractDto{
    private SimpleUserDto userOne;
    private SimpleUserDto userTwo;
    private String totalMatch;
    private String goalMatch;
    private String qualityMatch;
    private String stateMatch;
}
