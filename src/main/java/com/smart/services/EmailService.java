package com.smart.services;

import com.smart.entities.EmailDetails;

public interface EmailService {

    // Method
    // To send a simple email
    Boolean sendSimpleMail(EmailDetails details);

    // Method
    // To send an email with attachment
    String sendMailWithAttachment(EmailDetails details);
}
