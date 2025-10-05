package com.cuppa.CuppaApp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Table(name = "Users")
@Entity
@Getter
public class User {
    @Id
    @Column(name = "id")
    private Integer id;

    @Column
    private String name;
}
