package com.project.aptflow.unittest.service;

import com.project.aptflow.service.impl.EmailServiceImpl;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceUnitTest {
    @Mock
    private JavaMailSender javaMailSender;
    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailServiceImpl emailService;

    private byte[] pdfContent;
    private String testEmail;
    private String testSubject;
    private String testText;

    @BeforeEach
    void setUp() {
        pdfContent = new byte[0];
        testEmail = "testEmail";
        testSubject = "testSubject";
        testText = "testText";
    }

    @Nested
    @DisplayName("Send Email With PDF Attachment Test")
    class SendEmailWithPDFAttachmentTest {
        @Test
        @DisplayName("Send Email With PDF Attachment - Success")
        void sendBillWithPdf_SuccessTest() throws MessagingException {
            // Arrange
            when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
            // Act
            emailService.sendBillWithPdf(testEmail, testSubject, testText, pdfContent);
            // Assert
            verify(javaMailSender).createMimeMessage();
            verify(javaMailSender).send(mimeMessage);
        }

        @Test
        @DisplayName("Send Email With PDF Attachment - Failure")
        void sendBillWithPdf_FailureTest() {
            // Arrange
            when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
            doThrow(new MailSendException("Error sending email")).when(javaMailSender).send(any(MimeMessage.class));
            // Act and Assert
            assertThatThrownBy(() -> emailService.sendBillWithPdf(testEmail, testSubject, testText, pdfContent))
                    .isInstanceOf(MessagingException.class)
                    .hasMessage("Error sending email");
            // Verify
            verify(javaMailSender).send(mimeMessage);
        }
    }

    @Nested
    @DisplayName("Send Mail Test: Forget Password Link- Success")
    class SendMailTest {
        @Test
        @DisplayName("Send Mail - Success")
        void sendEmail_SuccessTest() throws MessagingException {
            // Arrange
            when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
            // Act
            emailService.sendEmail(testEmail, testSubject, testText);
            // Assert
            verify(javaMailSender).createMimeMessage();
            verify(javaMailSender).send(mimeMessage);
        }

        @Test
        @DisplayName("Send Mail - Failure")
        void sendEmail_FailureTest() {
            // Arrange
            when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
            doThrow(new MailSendException("Error sending email")).when(javaMailSender).send(any(MimeMessage.class));
            // Act and Assert
            assertThatThrownBy(() -> emailService.sendEmail(testEmail, testSubject, testText))
                    .isInstanceOf(MessagingException.class)
                    .hasMessage("Error sending email");
            // Verify
            verify(javaMailSender).send(mimeMessage);
        }
    }
}
