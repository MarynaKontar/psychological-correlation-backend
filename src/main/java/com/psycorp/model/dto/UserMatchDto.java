package com.psycorp.model.dto;

import lombok.Data;
import org.bson.types.ObjectId;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class UserMatchDto extends AbstractDto{
    private ObjectId id;

    //TODO пока на фронт посылаем SimpleUserDto, так как не ясно, какую еще информацию про пользователей Юра захочет выводить, но скорее всего, надо будет только имя пользователей
    @NotNull @Valid
    private List<SimpleUserDto> users;
    @NotNull @Valid
    private List<MatchingDto> matches;
    //    @NotEmpty @Valid
    //    private String advice; //совет по поводу результата совместимости респондентов
}