package com.project.aptflow.service.impl;

import com.project.aptflow.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender javaMailSender;

    public EmailServiceImpl(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void sendBillWithPdf(String to, String subject, String text, byte[] pdfContent) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,true);
        try {
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);
            ByteArrayResource pdfAttachment = new ByteArrayResource(pdfContent);
            helper.addAttachment("Rental_Bill_Details.pdf",pdfAttachment);
            javaMailSender.send(mimeMessage);
        }catch (Exception e){
            throw new MessagingException("Error sending email");
        }
    }

    @Override
    public void sendEmail(String email, String subject, String body) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,true);
        try {
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(body);
            javaMailSender.send(mimeMessage);
        }catch (Exception e){
            throw new MessagingException("Error sending email");
        }
    }
}
