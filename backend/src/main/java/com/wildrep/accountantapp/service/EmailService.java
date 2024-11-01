package com.wildrep.accountantapp.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    private String apiKey;

    public EmailService(@Value("${spring.sendgrid.api-key}") String apiKey) {
        this.apiKey = apiKey;
    }

    public String sendEmail(String toEmail, String subject, String body) {
        Email from = new Email("kichukamachka1@gmail.com");
        Email to = new Email(toEmail);

        Content content = new Content("text/html", body);
        Mail mail = new Mail(from, subject, to, content);
        SendGrid sendGrid = new SendGrid(this.apiKey);

        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sendGrid.api(request);

            return "Email sent to " + toEmail + " successfully!";
        } catch (IOException e) {
            return "Email sent to " + toEmail + " failed!";
        }
    }

}
