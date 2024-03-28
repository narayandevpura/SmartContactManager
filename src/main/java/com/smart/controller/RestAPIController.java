package com.smart.controller;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.jwt.authFilter.JwtAuthFilter;
import com.smart.jwt.model.JwtRequest;
import com.smart.jwt.model.JwtResponse;
import com.smart.jwt.service.JwtService;
import com.smart.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class RestAPIController {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ContactRepository contactRepository;

    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome this endpoint is not secure";
    }

    @PostMapping("/generateToken")
    public ResponseEntity<JwtResponse> authenticateAndGetToken(@RequestBody JwtRequest jwtRequest) {

        doAuthenticate(jwtRequest.getUsername(), jwtRequest.getPassword());

        UserDetails userDetails = userDetailsService.loadUserByUsername(jwtRequest.getUsername());
        String token = jwtService.generateToken(userDetails);

        JwtResponse response = new JwtResponse.builder()
                .withToken(token)
                .withUserName(userDetails.getUsername()).build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

//    method to authenticate
    private void doAuthenticate(String email, String password) {

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, password);

        try {

            authenticationManager.authenticate(authentication);

        } catch (BadCredentialsException e) {
            throw new BadCredentialsException(" Invalid Username or Password  !!");
        }

    }

    @ExceptionHandler(BadCredentialsException.class)
    public String exceptionHandler() {
        return "Credentials Invalid !!";
    }

// implementation is remaining
    @GetMapping("/view-contacts")
    public ResponseEntity<List<Contact>> view_contacts() {

        User user = userRepository.getUserByUserName(JwtAuthFilter.username);

        List<Contact> contactList = contactRepository.findByUser_Id(user.getId());

        if (!contactList.isEmpty())
            return new ResponseEntity<>(contactList, HttpStatus.OK);

        else return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

}
