package com.project.aptflow.service;

import jakarta.mail.MessagingException;

public interface EmailService {
    void sendBillWithPdf(String to,String subject,String text,byte[] pdfContent) throws MessagingException;

    void sendEmail(String email, String subject, String body) throws MessagingException;
}
