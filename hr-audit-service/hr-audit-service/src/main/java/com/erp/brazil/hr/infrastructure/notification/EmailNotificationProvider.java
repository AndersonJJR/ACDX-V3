package com.erp.brazil.hr.infrastructure.notification;

import com.erp.brazil.hr.application.usecase.SendNotificationUseCase;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationProvider implements SendNotificationUseCase.EmailNotificationProvider {

    private final JavaMailSender mailSender;

    @Value("${app.notifications.email.from:rh@empresa.com.br}")
    private String from;

    @Value("${app.notifications.email.reply-to:rh@empresa.com.br}")
    private String replyTo;

    @Override
    public void send(String to, String subject, String content) {
        if (to == null || to.isBlank()) {
            log.warn("Cannot send email: recipient is empty");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setReplyTo(replyTo);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true); // true = HTML

            mailSender.send(message);
            log.debug("Email sent to {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send email to {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    public void sendWithAttachment(String to, String subject, String content, byte[] attachment, String filename) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setReplyTo(replyTo);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            helper.addAttachment(filename, new ByteArrayResource(attachment));

            mailSender.send(message);
            log.debug("Email with attachment sent to {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send email with attachment to {}", to, e);
            throw new RuntimeException("Failed to send email with attachment", e);
        }
    }
}