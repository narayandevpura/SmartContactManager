package com.smart.controller;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    ContactRepository contactRepository;

    @ModelAttribute
    public void addCommonData(Model model, Principal principal){
        String userName = principal.getName();
        System.out.println("UserName "+ userName);

        User user = userRepository.getUserByUserName(userName);
        System.out.println(user);

        if (user.isEnabled()) {
            model.addAttribute("status", "ACTIVE");
        } else {
            model.addAttribute("status", "DEACTIVE");
        }
        model.addAttribute("user", user);
    }

    @RequestMapping("/index")
    public String dashboard(Model model) {
        model.addAttribute("title", "Dashboard - Smart Contact Manager");
        return "normal/user_dashboard";
    }

    @GetMapping("/add-contact")
    public String openAddContactForm(Model model) {
        model.addAttribute("title", "Add Contact - Smart Contact Manager");
        model.addAttribute("contact", new Contact());
        return "normal/add_contact_form";
    }

    @PostMapping("/process-contact")
    public String processContact(@ModelAttribute Contact contact,
                                 @RequestParam("profileImage") MultipartFile multipartFile,
                                 Principal principal,
                                 HttpSession session) throws IOException {
        try {
            String name = principal.getName();
            User user = userRepository.getUserByUserName(name);

            /* processing multipart file */

            if (multipartFile.isEmpty()) {
                /* setting default_image for contacts if no image is selected */
                contact.setImage("default_profile_image.png");
            } else {
                contact.setImage(multipartFile.getOriginalFilename());
                File file = new ClassPathResource("static/img").getFile();
                Path path = Paths.get(file.getAbsolutePath() + File.separator + multipartFile.getOriginalFilename());
                Files.copy(multipartFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            }

            /* processing ends here */

            contact.setUser(user);
            user.getContacts().add(contact);
            userRepository.save(user);
            session.setAttribute("message", new Message("Successfully added contact !!", "alert-success"));
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("message", new Message("Something went wrong !!", "alert-danger"));
        }

        return "normal/add_contact_form";
    }

    @GetMapping("/show-contacts/{page}")
    public String showContacts(@PathVariable("page") Integer page, Model model, Principal principal) {
        model.addAttribute("title", "View Contact - Smart Contact Manager");

        String userName = principal.getName();
        User user = userRepository.getUserByUserName(userName);
        PageRequest pageable = PageRequest.of(page, 2);
        Page<Contact> contacts = contactRepository.findByUser_id(user.getId(), pageable);
        if(page >= contacts.getTotalPages()) {
            model.addAttribute("message", new Message("Page index out of scope!!!", "alert-danger"));
        } else {
            model.addAttribute("contacts", contacts);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", contacts.getTotalPages());
        }
        return "normal/show_contacts";
    }

    @GetMapping("/{cId}/view_contact_info")
    public String showContactDetail(@PathVariable("cId") Integer cId, Model model, Principal principal) {
        Optional<Contact> contactOptional = contactRepository.findById(cId);
        Contact contact = contactOptional.get();

        String username = principal.getName();
        User user = userRepository.getUserByUserName(username);

        if (user.getId() == contact.getUser().getId()) {
            model.addAttribute("title", "Contact Detail");
            model.addAttribute("contact", contact);
        }
        else {
            model.addAttribute("title", "Error");
        }

        return "normal/contact_detail";
    }

    @GetMapping("/delete/{cId}")
    public String deleteContact(@PathVariable("cId") Integer cId, Model model, Principal principal) {
        Contact contact = contactRepository.findById(cId).get();

        String username = principal.getName();
        User user = userRepository.getUserByUserName(username);
        user.getContacts().remove(contact);
        userRepository.save(user);
        return "redirect:/user/show-contacts/0";
    }

    @GetMapping("/update-contact/{cId}")
    public String updateContact(@PathVariable("cId") Integer cId, Model model, Principal principal) {
        Contact contact = contactRepository.findById(cId).get();
        String username = principal.getName();
        User user = userRepository.getUserByUserName(username);

        if (user.getId() == contact.getUser().getId() && user != null) {
            model.addAttribute("title", "Update Contact - Smart Contact Manager");
            model.addAttribute("contact", contact);
        }
        else {
            model.addAttribute("title", "Error");
            model.addAttribute("message", new Message("You do not have this contact", "alert-danger"));
        }
        return "normal/update-form";
    }

    @PostMapping("/process-update")
    public String updateHandler(@ModelAttribute Contact contact,
                                @RequestParam("profileImage") MultipartFile multipartFile,
                                Principal principal,
                                HttpSession session) {

        System.out.println(contact);

        Contact oldContact = contactRepository.findById(contact.getcId()).get();
        try {

            if (!multipartFile.isEmpty()) {
                // delete old profile pic
                File deleteFile = new ClassPathResource("static/img").getFile();
                File deleteFile1 = new File(deleteFile, oldContact.getImage());
                if (!oldContact.getImage().equals("default_profile_image.png")){
                    deleteFile1.delete();
                }

                // update new profile pic
                File file = new ClassPathResource("static/img").getFile();
                Path path = Paths.get(file.getAbsolutePath() + File.separator + multipartFile.getOriginalFilename());
                Files.copy(multipartFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                contact.setImage(multipartFile.getOriginalFilename());

            } else {
                contact.setImage(oldContact.getImage());
            }
            String name = principal.getName();
            User user = userRepository.getUserByUserName(name);
            contact.setUser(user);
            contactRepository.save(contact);
            session.setAttribute("message", new Message("Successfully updated contact !!", "alert-success"));

        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("message", new Message("Something went wrong !!", "alert-danger"));
        }

            return "redirect:/user/" +contact.getcId()+ "/view_contact_info";
    }

    @GetMapping("/profile")
    public String yourProfile(Model model) {

        model.addAttribute("title", "User Profile - Smart Contact Manager");
        return "normal/profile";
    }
}
