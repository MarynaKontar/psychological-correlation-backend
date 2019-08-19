package com.psycorp.model.dto;

import com.psycorp.model.objects.UserMatch;
import lombok.Data;
import org.bson.types.ObjectId;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * DTO (Data Transfer Object) data level
 * for hiding implementation details of {@link UserMatch}.
 * @author Vitaliy Proskura
 * @author Maryna Kontar
 */
@Data
public class UserMatchDto extends AbstractDto{
    private ObjectId id;

    //TODO in this time send SimpleUserDto to frontend, because i don't know what information except user name Yura will want to output in frontend
    @NotNull @Valid
    private List<SimpleUserDto> users;
    @NotNull @Valid
    private List<MatchingDto> matches;
    //    @NotEmpty @Valid
    //    private String advice; //совет по поводу результата совместимости респондентов
}