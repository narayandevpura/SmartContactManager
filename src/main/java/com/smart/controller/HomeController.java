package com.smart.controller;

import com.smart.dao.UserRepository;
import com.smart.entities.EmailDetails;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.services.EmailServiceImpl;
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

    @Autowired
    private EmailServiceImpl emailService;

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

    @GetMapping("/forgot-password")
    public String forgotPassword(Model model) {
        model.addAttribute("title", "Forgot Password - Smart Contact Manager");
        return "forgotpasswordform";
    }

    @PostMapping("/send-otp")
    public String sendOTP(@RequestParam("email") String email, Model model, HttpSession session) {
        EmailDetails emailDetails = new EmailDetails();
        emailDetails.setRecipient(email);
        emailDetails.setSubject("One time password to Reser Password");
        String genOTP = emailService.generateOTP();
        emailDetails.setMsgBody(genOTP);

        Boolean isSent = emailService.sendSimpleMail(emailDetails);
        model.addAttribute("isSent", isSent);

        if (isSent) {
            session.setAttribute("email", email);
            session.setAttribute("isSent", isSent);
            session.setAttribute("genOTP", genOTP);
            session.setAttribute("message", new Message("Otp sent to " + email + " successfully!!!", "alert-success"));

            return "forgotpasswordform";
        } else {
            session.setAttribute("isSent", isSent);
            session.setAttribute("message", new Message("Otp could not be sent", "alert-danger"));

            return "forgotpasswordform";
        }
    }

    @PostMapping("/verify-otp")
    public String verifyOTP(@RequestParam("otp") String otp, Model model, HttpSession session) {
        if (session.getAttribute("genOTP").equals(otp)) {
            String email = session.getAttribute("email").toString();
            User userByUserName = userRepository.getUserByUserName(email);

            if (userByUserName==null){
                session.setAttribute("message", new Message("User with Email id " +email+ " not found!!!", "alert-danger"));
                return "forgotpasswordform";
            }
            if (email.equals(userByUserName.getEmail())){
                session.setAttribute("message", new Message("OTP verified!!!", "alert-success"));
                session.setAttribute("userRetrieved", userByUserName);
                model.addAttribute("resetP", true);
            }

        } else {
            session.setAttribute("message", new Message("Invalid OTP!!!", "alert-danger"));
            model.addAttribute("isSent", true);
        }
        return "forgotpasswordform";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam("newPassword") String password, HttpSession session) {
        User user = (User) session.getAttribute("userRetrieved");

        System.out.println("user retrieved is :" + user.getName() + ", " + user.getPassword());
        user.setPassword(bCryptPasswordEncoder.encode(password));
        userRepository.save(user);
        session.setAttribute("message", new Message("Password reset successfully!!!", "alert-success"));
        return "login";
    }

}

