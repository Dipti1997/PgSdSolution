package com.email.send.grid.controller;

import com.email.send.grid.dto.SendEmailDto;
import com.email.send.grid.service.SendEmailWithAlibabaService;
import com.email.send.grid.service.SendEmailWithSendgridService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/alibaba")
public class SendEmailWithAlibabaCloudController {

    @Autowired
    SendEmailWithAlibabaService alibabaService;

    // This method is used to send email using alibaba cloud
    @PostMapping("/send-email")
    public String sendEmail(@RequestBody SendEmailDto emailDto) {
        return alibabaService.sendEmailUsingAlibabaSmtp(emailDto);
    }
}
