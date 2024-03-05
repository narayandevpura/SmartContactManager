package com.smart.controller;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class HomeController {

    @Autowired
    private UserRepository userRepository;

    @RequestMapping("/")
    public String home(Model model) {

        model.addAttribute("title", "Home - Smart Contact Manager");
        return "home";
    }

    @RequestMapping("/about")
    public String about(Model model) {

        model.addAttribute("title", "About - Smart Contact Manager");
        return "about";
    }

    @RequestMapping("/signup")
    public String signup(Model model) {

        model.addAttribute("title", "Register - Smart Contact Manager");
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/do_register")
    public String registerUser(@ModelAttribute("user") User user, @RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model, HttpSession session) {

        try {
            if (!agreement) {
                throw new Exception("Please agree to the terms and conditions");
            }

            user.setRole("ROLE_USER");
            user.setEnabled(true);
            user.setImageUrl("default.png");

//        System.out.println("Agreement" + agreement);
//        System.out.println("USER" + user);

            User result = userRepository.save(user);

            model.addAttribute("user", new User());

            session.setAttribute("message", new Message("Successfully Registered !!", "alert-success"));
            return "signup";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("user", user);
            session.setAttribute("message", new Message("Something went wrong !!"+e.getMessage(), "alert-danger"));
            return "signup";
        }
    }
}

