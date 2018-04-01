package com.psycorp.model.entity;

import lombok.Data;

import java.util.Set;

@Data
public class UserAnswers extends AbstractEntity{
    private User user;
    private Set<Choice> userAnswers;
}
