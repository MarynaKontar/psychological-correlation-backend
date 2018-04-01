package com.psycorp.model.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@MappedSuperclass
public abstract class AbstractEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
}
