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
            Event event = new Event();
                    event.setDescription(description);
                    event.setStartDateTime(LocalDateTime.parse(startDateTime));
                    event.setEndDateTime(LocalDateTime.parse(endDateTime));
                    event.setUser(users.findFirstByName(userName));

            List<Event> notBusy = events.findAllByUser(event.getUser()).stream().
                    filter(e -> (
                            (event.getStartDateTime().isAfter(e.getStartDateTime()) && event.getEndDateTime().isBefore(e.getEndDateTime()))
                            ||
                            (event.getStartDateTime().isBefore(e.getStartDateTime()) && event.getEndDateTime().isAfter(e.getEndDateTime()))
                            ||
                            (event.getStartDateTime().isBefore(e.getStartDateTime()) && event.getEndDateTime().isAfter(e.getStartDateTime()))
                            ||
                            (event.getStartDateTime().isBefore(e.getEndDateTime()) && event.getEndDateTime().isAfter(e.getEndDateTime()))
                            )).collect(Collectors.toList());

            if (notBusy.size() == 0) {
                events.save(event);
            }else {
                throw new Exception("Dude, your busy!" + notBusy.toString());
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