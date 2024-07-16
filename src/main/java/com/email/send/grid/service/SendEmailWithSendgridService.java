package com.email.send.grid.service;

import com.email.send.grid.dto.SendEmailDto;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;
import com.email.send.grid.constants.EmailConfigConstants;

@Service
public class SendEmailWithSendgridService {


    public String sendgridSendEmail(SendEmailDto emailDto) {
        Email fromEmail = new Email(emailDto.getFromEmail());
        fromEmail.setName(emailDto.getFromName());
        Content content = new Content(emailDto.getEmailType(), emailDto.getBody());
        Mail mail = new Mail();
        mail.setFrom(fromEmail);
        mail.setSubject(emailDto.getSubject());
        mail.addContent(content);
        Integer statusCode = null;
        Set<String> toMails = new TreeSet<>();
        if (emailDto.getTestEmail() != null && !emailDto.getTestEmail().isEmpty()) {
            toMails.add(emailDto.getTestEmail());
        } else {
            toMails = Arrays.stream(emailDto.getBulkEmail().split(",")).
                    collect(Collectors.toSet());
        }
        for (String toMail : toMails) {
            Email toEmail = new Email(toMail);
            Personalization personalization = new Personalization();
            personalization.addTo(toEmail);
            mail = new Mail(fromEmail, emailDto.getSubject(), toEmail, content);
            mail.addPersonalization(personalization);

            SendGrid sg = null;
            if (emailDto.apiKey != null && !emailDto.apiKey.isEmpty()) {
                sg = new SendGrid(emailDto.apiKey);
            }

            Request request = new Request();

            try {
                request.setMethod(Method.POST);
                request.setEndpoint("mail/send");
                request.setBody(mail.build());
                Response response = sg.api(request);
                statusCode = response.getStatusCode();
            } catch (IOException ex) {
                return "Error: " + ex.getMessage();
            }
        }
        return statusCode == 202 ? "Email Sent Successfully." :
                "There is some issue with sending email, please try again later.";
    }
}
