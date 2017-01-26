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
    public String createEvent(HttpSession session, String description, String startDateTime, String endDateTime) throws Exception {
        String userName = (String) session.getAttribute("userName");
        if (userName != null) {
            Event newEvent = new Event(
                    description,
                    LocalDateTime.parse(startDateTime),
                    LocalDateTime.parse(endDateTime),
                    users.findFirstByName(userName));
            if (LocalDateTime.parse(startDateTime).isBefore(LocalDateTime.parse(endDateTime))) {
               List<Event> collidingEvents = events.findAllByUserOrderByStartDateTimeDesc(users.findFirstByName(userName)).stream()
                        .filter(e -> checkDateAndTime(newEvent, e) == false)
                        .collect(Collectors.toList());
               if (collidingEvents.size() == 0) {
                   events.save(newEvent);
               } else {
                        throw new Exception("Um, do you have a Time Turner? No? Then you can't be in two places at once.");
                             //   "Conflicting appointment: \n" + collidingEvents);
                    }

            } else {
                throw new Exception("End time of event must be AFTER the start time, dummy.");
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

    public static boolean checkDateAndTime(Event newEvent, Event existingEvent) {
        if ((newEvent.startDateTime.isAfter(existingEvent.startDateTime) && newEvent.endDateTime.isBefore(existingEvent.endDateTime)) ||
            (newEvent.startDateTime.isBefore(existingEvent.startDateTime) && newEvent.endDateTime.isAfter(existingEvent.endDateTime)) ||
            (newEvent.startDateTime.isBefore(existingEvent.startDateTime) && newEvent.endDateTime.isAfter(existingEvent.startDateTime))||
            (newEvent.startDateTime.isBefore(existingEvent.endDateTime) && newEvent.endDateTime.isAfter(existingEvent.endDateTime))){
            return false;
        } else {
            return true;
        }
    }
}