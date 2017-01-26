package com.theironyard.entities;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue
    int id;

    @Column(nullable = false)
    String description;

    @Column(nullable = false, unique = true)
    public
    LocalDateTime startDateTime;

    @Column(nullable = false, unique = true)
    public
    LocalDateTime endDateTime;

    @ManyToOne
    User user;

    public Event() {
    }

    public Event(String description, LocalDateTime startDateTime, LocalDateTime endDateTime, User user) {
        this.description = description;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.user = user;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}