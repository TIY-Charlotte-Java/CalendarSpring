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

    //List<Event> eventEntities = new ArrayList<>();

    @RequestMapping(path = "/", method = RequestMethod.GET)
    public String home(Model model, HttpSession session) {
        String userName = (String) session.getAttribute("userName");
        List<Event> eventEntities = new ArrayList<>();
        //if user name is not null then go and find a user by the below quiry
        if (userName != null) {
            User user = users.findFirstByName(userName);

            if (user != null) {
                eventEntities = events.findAllByUserOrderByStartTimeDesc(user);
            }
            model.addAttribute("user", user);
            model.addAttribute("now", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        }
        model.addAttribute("events", eventEntities);
        return "home";
    }


    @RequestMapping(path = "/create-event", method = RequestMethod.POST)
    public String createEvent(HttpSession session, String description, String startTime, String endTime) throws Exception {
        String userName = (String) session.getAttribute("userName");

        if (userName != null) {
            Event newEvent = new Event(description,
                    LocalDateTime.parse(startTime),
                    LocalDateTime.parse(endTime),
                    users.findFirstByName(userName));

            if (LocalDateTime.parse(startTime).isBefore(LocalDateTime.parse(endTime))) {
                List<Event> sameEvent = events.findAllByUserOrderByStartTimeDesc(users.findFirstByName(userName))
                        .stream().filter(e -> check(newEvent, e) == false)
                        .collect(Collectors.toList());
                if (sameEvent.size() == 0) {
                    events.save(newEvent);
                } else {
                    throw new Exception("you can't be at two places" + sameEvent.toString());
                }
            } else {
                throw new Exception("not valid end time ");
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

    public static boolean check(Event newEvent, Event existingEvent) {
        if ((newEvent.startTime.isAfter(existingEvent.startTime) && newEvent.endTime.isBefore(existingEvent.endTime)) ||
                (newEvent.startTime.isBefore(existingEvent.startTime) && newEvent.endTime.isAfter(existingEvent.endTime)) ||
                (newEvent.startTime.isBefore(existingEvent.startTime) && newEvent.endTime.isAfter(existingEvent.startTime)) ||
                (newEvent.startTime.isBefore(existingEvent.endTime) && newEvent.endTime.isAfter(existingEvent.endTime))) {
            return false;
        } else

        {
            return true;


        }
    }
}