package com.project.aptflow.scheduler;

import com.project.aptflow.entity.GenerateBillEntity;
import com.project.aptflow.enums.DeliveryStatus;
import com.project.aptflow.pdf.RentalBillPdfGenerator;
import com.project.aptflow.repository.GenerateBillRepository;
import com.project.aptflow.service.EmailService;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Component
public class EmailScheduler {
    private static final Logger logger = LoggerFactory.getLogger(EmailScheduler.class);
    private final Clock clock;

    private final GenerateBillRepository generateBillRepository;
    private final EmailService emailService;
    private final RentalBillPdfGenerator rentalBillPdfGenerator;

    public EmailScheduler(Clock clock, GenerateBillRepository generateBillRepository, EmailService emailService, RentalBillPdfGenerator rentalBillPdfGenerator) {
        this.clock = clock;
        this.generateBillRepository = generateBillRepository;
        this.emailService = emailService;
        this.rentalBillPdfGenerator = rentalBillPdfGenerator;
    }

    @Scheduled(cron = "0 0 4 1 * *")
    public void sendBillsOnMonthEnd(){
        YearMonth lastMonth = YearMonth.now(clock).minusMonths(1);
        List<GenerateBillEntity> generateBillEntities = generateBillRepository.findByMonthAndStatus(lastMonth, DeliveryStatus.NOT_SENT);
        for (GenerateBillEntity bill : generateBillEntities){
            try {
                byte[] pdfContent = rentalBillPdfGenerator.generateBillPdf(bill);
                emailService.sendBillWithPdf(bill.getUserEntity().getEmail()," Monthly Bill Details.",
                "Hi "+bill.getUserEntity().getName()+ "\n\nPlease find your attached bill details."+lastMonth,pdfContent);
                bill.setDeliveryStatus(DeliveryStatus.SENT);
                generateBillRepository.save(bill);
            }catch (MessagingException | IOException e){
                logger.error("Error sending email: {}", e.getMessage(), e);
            }
        }
    }

    @Scheduled(cron = "0 0 18 * * *")
    public void sendBillsOnCheckOut(){
        LocalDate today = LocalDate.now(clock);
        List<GenerateBillEntity> generateBillEntities = generateBillRepository.findByCheckOutDate(today, DeliveryStatus.NOT_SENT);
        for (GenerateBillEntity bill : generateBillEntities){
            try {
                byte[] pdfContent = rentalBillPdfGenerator.generateBillPdf(bill);
                emailService.sendBillWithPdf(
                        bill.getUserEntity().getEmail(),
                        "Rental Bill Details.",
                        "Hi "+bill.getUserEntity().getName()+"\n\nPlease find your attached bill details.",
                        pdfContent);
                bill.setDeliveryStatus(DeliveryStatus.SENT);
                generateBillRepository.save(bill);
            }catch (MessagingException | IOException e){
                logger.error("Error sending email: {}", e.getMessage(), e);
            }
        }
    }
}
