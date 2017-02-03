package com.theironyard.controllers;

import com.theironyard.entities.Event;
import com.theironyard.entities.User;
import com.theironyard.services.EventRepository;
import com.theironyard.services.UserRepository;
import org.apache.tomcat.jni.Local;
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
                eventEntities = events.findAllByUserOrderByStartDateTimeDesc(user);
            }

            model.addAttribute("user", user);
            model.addAttribute("now", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        }

        model.addAttribute("events", eventEntities);
        return "home";
    }

    @RequestMapping(path = "/create-event", method = RequestMethod.POST)
    public String createEvent(HttpSession session, String description, String startDateTime, String endDateTime) throws Exception{
        String userName = (String) session.getAttribute("userName");
        if (userName != null) {
            Event event = new Event(description, LocalDateTime.parse(startDateTime), LocalDateTime.parse(endDateTime), users.findFirstByName(userName));
            events.save(event);
            Event event1 = new Event(description, LocalDateTime.parse(startDateTime), LocalDateTime.parse(endDateTime), users.findFirstByName(userName));
            if (LocalDateTime.parse(startDateTime).isBefore(LocalDateTime.parse(endDateTime))) {
                List<Event> collisionEvent = events.findAllByUserOrderByStartDateTimeDesc(users.findFirstByName(userName)).stream().filter(e -> checkDateAndTime(event1, e) == false)
                        .collect(Collectors.toList());
                if (collisionEvent.size() == 0) {
                    events.save(event1);
                } else {
                    throw new Exception("You already have something scheduled in this time slot.");
                }
            } else {
                throw new Exception("End time must be after start time.");
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

    public static boolean checkDateAndTime (Event newEvent, Event existing) {
        if ((newEvent.startDateTime.isAfter(existing.startDateTime) && newEvent.endDateTime.isBefore(existing.endDateTime)) ||
                (newEvent.startDateTime.isBefore(existing.startDateTime) && newEvent.endDateTime.isAfter(existing.endDateTime)) ||
                (newEvent.startDateTime.isBefore(existing.startDateTime) && newEvent.endDateTime.isAfter(existing.startDateTime)) ||
                (newEvent.startDateTime.isBefore(existing.endDateTime) && newEvent.endDateTime.isAfter(existing.endDateTime))) {
            return false;
        } else {
            return true;
        }
    }
}