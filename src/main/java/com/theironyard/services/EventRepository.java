package com.theironyard.services;

import com.theironyard.entities.Event;
import com.theironyard.entities.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by Ben on 1/25/17.
 */
public interface EventRepository extends CrudRepository<Event, Integer> {
    List<Event> findAllByUserOrderByStartTimeDesc(User user);
    List<Event> findAllByUser(User user);
}
