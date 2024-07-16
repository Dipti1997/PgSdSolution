package com.email.send.grid.controller;

import com.email.send.grid.dto.SendEmailDto;
import com.email.send.grid.service.SendEmailWithSendgridService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sendgrid")
public class SendEmailWithSendGridController {
    @Autowired
    private SendEmailWithSendgridService sendEmailWithSendgridService;

    @PostMapping("/send-email")
    public String sendEmail(@RequestBody SendEmailDto emailDto) {
        return sendEmailWithSendgridService.sendgridSendEmail(emailDto);
    }
}
