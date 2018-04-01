package com.psycorp.model.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Набор попарных сравнений по трем полям, по шесть шкал в каждом поле для каждого юзера
 */

@Data
@Entity
@Table(name = "user_answer")
public class UserAnswers extends AbstractEntity{
    private User user;
    private Set<Choice> userAnswers;
    private LocalDateTime creationDate;
    private LocalDateTime passDate;
}
