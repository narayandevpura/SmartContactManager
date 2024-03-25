package com.smart.controller;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Controller
public class HomeController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

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
    public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult bindingResult,
                               @RequestParam(value = "agreement", defaultValue = "false") boolean agreement,
                               @RequestParam("userImage") MultipartFile multipartFile,
                               Model model, HttpSession session) {

        try {
            if (!agreement) {
                throw new Exception("Please agree to the terms and conditions");
            }

            if (bindingResult.hasErrors()) {
                System.out.println("Error "+ bindingResult.toString());
                model.addAttribute("user", user);
                model.addAttribute("agreement", agreement);
                return "signup";
            }
            user.setRole("ROLE_USER");
            user.setEnabled(true);

            /* processing multipart file */

            if (multipartFile.isEmpty()) {
                /* setting default_image for contacts if no image is selected */
                user.setImageUrl("default_profile_image.png");
            } else {
                user.setImageUrl(multipartFile.getOriginalFilename());
                File file = new ClassPathResource("static/img").getFile();
                Path path = Paths.get(file.getAbsolutePath() + File.separator + multipartFile.getOriginalFilename());
                Files.copy(multipartFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            }

            /* processing ends here */
            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));

//        System.out.println("Agreement" + agreement);
//        System.out.println("USER" + user);

            User result = userRepository.save(user);

            model.addAttribute("user", new User());
            model.addAttribute("agreement", agreement);
            session.setAttribute("message", new Message("Successfully Registered !!", "alert-success"));
            return "signup";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("user", user);
            model.addAttribute("agreement", agreement);
            session.setAttribute("message", new Message("Something went wrong !!"+e.getMessage(), "alert-danger"));
            return "signup";
        }
    }

    @GetMapping("/signin")
    public String customLogin(Model model) {
        model.addAttribute("title", "Login - Smart Contact Manager");
        return "login";
    }
}

