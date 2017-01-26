package com.theironyard.controllers;

import com.theironyard.entities.Event;
import com.theironyard.entities.User;
import com.theironyard.services.EventRepository;
import com.theironyard.services.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class CalendarSpringController {
    @Autowired
    EventRepository events;

    @Autowired
    UserRepository users;

    @RequestMapping(path = "/", method = RequestMethod.GET)
    public String home(Model model, HttpSession session) {
        String userName = (String) session.getAttribute("userName");
        List<Event> eventEntities = new ArrayList<>();

        if (userName != null) {
            User user = users.findFirstByName(userName);

            if (user != null) {
                eventEntities = events.findByUsersIn(Stream.of(user).collect(Collectors.toList()));
            }

            model.addAttribute("user", user);
            model.addAttribute("now", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        }

        model.addAttribute("events", eventEntities);
        return "home";
    }

    @RequestMapping(path = "/create-event", method = RequestMethod.POST)
    public String createEvent(HttpSession session, String description, String startTime, String endTime, String attendee) {
        String userName = (String) session.getAttribute("userName");

        // get current user
        User currentUser = users.findFirstByName(userName);

        // get other user
        User otherUser = users.findFirstByName(attendee);

        // for each user, make sure they have no colliding events
        // if not, save event to each user.

        if (currentUser != null) {
            Event event = new Event(description, LocalDateTime.parse(startTime), LocalDateTime.parse(endTime), Stream.of(users.findFirstByName(userName)).collect(Collectors.toList()));

            if (!event.collidesWithEvents(currentUser)) {
                if (otherUser != null) {
                    if (!event.collidesWithEvents(otherUser)) {
                        events.save(event);
                        otherUser.getEvents().add(event);
                        currentUser.getEvents().add(event);
                        users.save(currentUser);
                        users.save(otherUser);
                    }
                } else {
                    if (!event.collidesWithEvents(currentUser)){
                        events.save(event);
                        currentUser.getEvents().add(event);
                        users.save(currentUser);
                    }
                }
            }
        }

        return "redirect:/";
    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(HttpSession session, String name) {
        User user = users.findFirstByName(name);
        if (user == null) {
            user = new User(name);
            users.save(user);
        }
        session.setAttribute("userName", name);
        return "redirect:/";
    }

    @RequestMapping(path = "/logout", method = RequestMethod.POST)
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}