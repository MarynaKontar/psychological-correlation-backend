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
    private ObjectId id;
    private List<SimpleUserDto> users;
    private List<Match> matches;
    private String advice; //совет по поводу результата совместимости респондентов
}
