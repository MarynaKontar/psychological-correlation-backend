package com.psycorp.model.dto;

import com.psycorp.model.entity.Match;
import com.psycorp.model.enums.MatchMethod;
import lombok.Data;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class UserMatchDto extends AbstractDto{
//    private ObjectId id;
    private List<SimpleUserDto> users;
    //TODO Что лучше users или userNames?
//    private Set<String> userNames;
//    private SimpleUserDto userOne;
//    private SimpleUserDto userTwo;

    private List<Match> matches;
//    private List<Match> matches;
//    private MatchMethod matchMethod;
//    private String totalMatch;
//    private String goalMatch;
//    private String qualityMatch;
//    private String stateMatch;

    private String advice; //совет по поводу результата совместимости респондентов
}
