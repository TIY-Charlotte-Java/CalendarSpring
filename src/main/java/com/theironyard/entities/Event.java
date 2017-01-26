package com.theironyard.entities;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue
    int id;

    @Column(nullable = false)
    String description;

    @Column(nullable = false)
    LocalDateTime startTime;

    @Column(nullable = false)
    LocalDateTime endTime;

    @ManyToMany(mappedBy = "events")
    List<User> users;

    public Event() {
    }

    public Event(String description, LocalDateTime startTime, LocalDateTime endTime, List<User> users) {
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.users = users;
    }

    public boolean collidesWithEvents(User user) {
        return user.getEvents()
            .stream()
            .filter(this::overlaps)
            .count() > 0;
    }

    public boolean overlaps(Event e) {
        return !(
                (startTime.isBefore(e.startTime) && endTime.isBefore(e.startTime)) ||
                (startTime.isAfter(e.endTime)));
    }
}