package com.email.send.grid.service;

import com.email.send.grid.common.CommonUtils;
import com.email.send.grid.constants.EmailConfigConstants;
import com.email.send.grid.dto.SendEmailDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

@Service
public class SendEmailWithAlibabaService {
    @Value("${azure.storage.account-name}")
    private String azureStorageAccountName;

    @Value("${azure.storage.account-key}")
    private String azureStorageAccountKey;

    @Value("${azure.storage.container-name}")
    private String azureStorageContainerName;

    @Value("${azure.storage.temp-container-name}")
    private String destinationContainer;
    @Autowired
    private CommonUtils commonUtils;

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
        // String encodedBody = Base64Encoder.encode(emailDto.getBody());
            if (emailDto.getTestEmail() != null && !emailDto.getTestEmail().isEmpty() && emailDto.emailType.equals("test")) {
                String toAddress = emailDto.testEmail; // Single recipient for single email
                sendSingleEmail(session, emailDto.getFromEmail(), emailDto.getFromName(), toAddress,
                        emailDto.getSubject(), emailDto.getBody());
            } else if (emailDto.getBulkEmail() == null || !emailDto.getBulkEmail().isEmpty() && emailDto.emailType.equals("bulk")) {
                List<String> uiRecipients =
                        Arrays.asList(emailDto.getBulkEmail().split(",")); // Multiple recipients for batch email
                List<String> tempRecipients = null;
                try {
                    tempRecipients = commonUtils.updateCsvFile(emailDto.dataFile, emailDto.limit,azureStorageAccountName,azureStorageAccountKey,destinationContainer);
                }catch (Exception e){
                    return "Failed to fetch data from Temp file: "+ emailDto.dataFile;
                }
                uiRecipients.addAll(tempRecipients);
                boolean ismailSuccess = sendBatchEmail(session, emailDto.getFromEmail(), emailDto.getFromName(),
                        emailDto.getSubject(), emailDto.getBody(), uiRecipients);
                if (ismailSuccess){
                    // TODO remove mails from temp file
                }
            } else {
                System.out.println("Invalid email type specified. Use 'single' or 'batch'.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Something went wrong while sending email.";
        }
        return "Email Sent Successfully using Alibaba Cloud SMTP.";
    }

    private static void sendSingleEmail(Session session, String fromAlias, String fromName,
                                        String toAddress, String subject, String body)
            throws UnsupportedEncodingException, MessagingException {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromAlias, fromName));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress));
        message.setSubject(subject);

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

    private static boolean sendBatchEmail(Session session, String fromAlias, String fromName,
                                       String subject, String body, List<String> recipients)
            throws MessagingException, UnsupportedEncodingException {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromAlias, fromName));
        message.setSubject(subject);

        // Create a multipart message for HTML content
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(body, "text/html; charset=utf-8");
        mimeBodyPart.addHeader("Content-Transfer-Encoding", "base64");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        // Set the multipart message to the email message
        message.setContent(multipart);

        for (String toAddress : recipients) {
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress));
            Transport.send(message);
            System.out.println("Batch email sent to " + toAddress);
        }
        return true;
    }
}
