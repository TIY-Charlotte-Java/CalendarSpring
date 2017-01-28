package com.theironyard.entities;

import javax.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    public int id;

    @Column(nullable = false, unique = true)
    public String name;

    public User() {
    }

    public User(String name) {
        this.name = name;
    }
}