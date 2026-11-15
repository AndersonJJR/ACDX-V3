package com.erp.brazil.hr.infrastructure.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Component
public class EmailProvider implements NotificationProvider {

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public EmailProvider(JavaMailSender mailSender,
                         @Value("${spring.mail.username:noreply@erp-brazil.com}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Override
    public String send(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true); // HTML
            mailSender.send(message);
            return message.getMessageID();
        } catch (MessagingException e) {
            throw new NotificationException("Failed to send email", e);
        }
    }

    @Override
    public Notification.Channel getChannel() {
        return Notification.Channel.EMAIL;
    }
}