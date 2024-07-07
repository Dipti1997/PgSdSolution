package com.email.send.grid.controller;

import com.email.send.grid.dto.SendEmailDto;
import com.email.send.grid.service.SendEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api/v1/sendgrid")
public class SendEmailWithSendGridController {
    @Autowired
    private SendEmailService sendEmailService;

    @PostMapping("/send-email")
    public String sendEmail(@RequestBody SendEmailDto emailDto) {
        return sendEmailService.sendEmail(emailDto);
    }
}
