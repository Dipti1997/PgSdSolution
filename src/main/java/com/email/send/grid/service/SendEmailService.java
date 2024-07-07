package com.email.send.grid.service;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dm.model.v20151123.BatchSendMailRequest;
import com.aliyuncs.dm.model.v20151123.BatchSendMailResponse;
import com.aliyuncs.dm.model.v20151123.SingleSendMailRequest;
import com.aliyuncs.dm.model.v20151123.SingleSendMailResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.email.send.grid.config.Base64Encoder;
import com.email.send.grid.dto.SendEmailDto;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import org.springframework.beans.factory.annotation.Value;
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
public class SendEmailService {


    public String sendEmail(SendEmailDto emailDto) {
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

    public String sendEmailUsingAlibabaCloud(SendEmailDto emailDto) {
        // Code to send email using Alibaba Cloud
        String regionId = null;
        if (emailDto.getZone().equals("CHINA")) {
            regionId = EmailConfigConstants.REGION_ID_CHINA;
        } else if (emailDto.getZone().equals("AUS")) {
            regionId = EmailConfigConstants.REGION_ID_AUS;
        } else if (emailDto.getZone().equals("SINGAPORE")) {
            regionId = EmailConfigConstants.REGION_ID_SINGAPOOR;
        } else {
            regionId = EmailConfigConstants.REGION_ID_SINGAPOOR;
        }
//        if (emailDto.getAccessId() != null && !emailDto.getAccessId().isEmpty()) {
//            accesskeyId = emailDto.getAccessId();
//        }
//        if (emailDto.getAccessKey() != null && !emailDto.getAccessKey().isEmpty()) {
//            accessKeySecret = emailDto.getAccessKey();
//        }
        IClientProfile profile = DefaultProfile.getProfile(regionId, emailDto.getAccessId(), emailDto.getAccessKey());
        IAcsClient client = new DefaultAcsClient(profile);

        try {
            if (emailDto.getTestEmail() != null && !emailDto.getTestEmail().isEmpty()) {
                SingleSendMailRequest request = new SingleSendMailRequest();
                request.setAccountName(emailDto.getFromEmail());
                request.setFromAlias(emailDto.fromName);
                request.setAddressType(1);
                request.setReplyToAddress(true);
                request.setToAddress(emailDto.getTestEmail());
                request.setSubject(emailDto.getSubject());
                request.setHtmlBody(emailDto.getBody());
                SingleSendMailResponse response = client.getAcsResponse(request);
                System.out.println("Single email sent! RequestId: " + response.getRequestId());
            } else {
                BatchSendMailRequest request = new BatchSendMailRequest();
                request.setAccountName(emailDto.getFromEmail());
                request.setAddressType(1);
                request.setTemplateName("Dipti_test");
                request.setReceiversName(emailDto.getBulkEmail());
                BatchSendMailResponse response = client.getAcsResponse(request);
                System.out.println("Batch email sent! RequestId: " + response.getRequestId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Email Sent Successfully using Alibaba Cloud.";
    }


    public String sendEmailUsingAlibabaSmtp(SendEmailDto emailDto) {
        String smtpHost = null;
        if(emailDto.getZone().equals("CHINA")) {
            smtpHost = EmailConfigConstants.SMTP_ENDPOINT_EAST_CHINA;
        } else if(emailDto.getZone().equals("US")) {
            smtpHost = EmailConfigConstants.SMTP_ENDPOINT_US_EAST;
        } else if(emailDto.getZone().equals("SINGAPORE")) {
            smtpHost = EmailConfigConstants.SMTP_ENDPOINT_SINGAPORE;
        } else if(emailDto.getZone().equals("GERMANY")) {
            smtpHost = EmailConfigConstants.SMTP_ENDPOINT_GERMANY;
        }
        // Code to send email using Alibaba Cloud SMTP
        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.socketFactory.port", EmailConfigConstants.SMTP_PORT);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", EmailConfigConstants.SMTP_PORT);

        Session session = Session.getDefaultInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailDto.getAccessId(), emailDto.getAccessKey());
            }
        });
        try {
//            String encodedBody = Base64Encoder.encode(emailDto.getBody());
            if (emailDto.getTestEmail() != null && !emailDto.getTestEmail().isEmpty()) {
                String toAddress = emailDto.testEmail; // Single recipient for single email
                sendSingleEmail(session, emailDto.getFromEmail(), emailDto.getFromName(), toAddress, emailDto.getSubject(), emailDto.getBody());
            } else if (emailDto.getTestEmail() == null || emailDto.getTestEmail().isEmpty()) {
                List<String> recipients = Arrays.asList(emailDto.getBulkEmail().split(",")); // Multiple recipients for batch email
                sendBatchEmail(session, emailDto.getFromEmail(), emailDto.getFromName(), emailDto.getSubject(), emailDto.getBody(), recipients);
            } else {
                System.out.println("Invalid email type specified. Use 'single' or 'batch'.");
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return "Email Sent Successfully using Alibaba Cloud SMTP.";
    }

    private static void sendSingleEmail(Session session, String fromAlias, String fromName, String toAddress, String subject, String body) throws UnsupportedEncodingException, MessagingException {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromAlias, fromName));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress));
        message.setSubject(subject);
//        message.setText(body);
//        message.setContent(body, "text/html; charset=utf-8");

        // Create a multipart message for HTML content
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(body, "text/html; charset=utf-8");
        mimeBodyPart.addHeader("Content-Transfer-Encoding", "base64");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        // Set the multipart message to the email message
        message.setContent(multipart);

        Transport.send(message);
        System.out.println("Single email sent to " + toAddress);
    }

    private static void sendBatchEmail(Session session, String fromAlias, String fromName, String subject, String body, List<String> recipients) throws MessagingException, UnsupportedEncodingException {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromAlias, fromName));
        message.setSubject(subject);
//        message.setText(body);
//        message.setContent(body, "text/html; charset=utf-8");

        // Create a multipart message for HTML content
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(body, "text/html; charset=utf-8");
        mimeBodyPart.addHeader("Content-Transfer-Encoding", "base64");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        // Set the multipart message to the email message
        message.setContent(multipart);

        for (String recipient : recipients) {
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            Transport.send(message);
            System.out.println("Batch email sent to " + recipient);
        }
    }
}
