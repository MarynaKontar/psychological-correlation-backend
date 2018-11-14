package com.psycorp.model.entity;

import com.psycorp.model.enums.Scale;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValueProfile {
    private Map<Scale, ValueProfileComment> scaleResult;
    private Boolean isPrincipalUser;
}
