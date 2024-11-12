package com.wildrep.accountantapp.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

@Service
@Slf4j
public class EmailService {

    private final String apiKey;

    public EmailService(@Value("${spring.sendgrid.api-key}") String apiKey) {
        this.apiKey = apiKey;
    }

    public void sendEmail(String toEmail, String subject, String body, File attachment) {
        Email from = new Email("kichukamachka1@gmail.com");
        Email to = new Email(toEmail);
        try {
        Content content = new Content("text/html", body);
        Mail mail = new Mail(from, subject, to, content);

        if (attachment != null) {
            Attachments attachments = new Attachments();
            attachments.setFilename(attachment.getName());
            attachments.setType("application/pdf");
            attachments.setDisposition("attachment");
            attachments.setContent(Base64.getEncoder().encodeToString(Files.readAllBytes(attachment.toPath())));
            mail.addAttachments(attachments);
        }
        SendGrid sendGrid = new SendGrid(this.apiKey);

        Request request = new Request();


            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sendGrid.api(request);

        } catch (IOException e) {
            log.error("Failed to load attachment or to send the email! Message: {}" ,e.getMessage());
        }
    }

}
