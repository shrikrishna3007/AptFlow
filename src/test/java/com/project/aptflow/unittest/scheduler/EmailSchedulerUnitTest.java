package com.project.aptflow.unittest.scheduler;

import com.project.aptflow.entity.*;
import com.project.aptflow.enums.DeliveryStatus;
import com.project.aptflow.enums.GenerateBillStatus;
import com.project.aptflow.pdf.RentalBillPdfGenerator;
import com.project.aptflow.repository.GenerateBillRepository;
import com.project.aptflow.scheduler.EmailScheduler;
import com.project.aptflow.service.EmailService;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailSchedulerUnitTest {
    @Mock private Clock clock;
    @Mock private GenerateBillRepository generateBillRepository;
    @Mock private EmailService emailService;
    @Mock private RentalBillPdfGenerator rentalBillPdfGenerator;

    @InjectMocks
    private EmailScheduler emailScheduler;

    private final LocalDate fixedDate = LocalDate.of(2025,6,1);
    private final ZoneId zoneId = ZoneId.systemDefault();

    @BeforeEach
    void setup() {
        Clock fixedClock = Clock.fixed(fixedDate.atStartOfDay(zoneId).toInstant(),zoneId);
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(zoneId);
    }

    private GenerateBillEntity createGenerateBillEntity() {
        GenerateBillEntity generateBillEntity = new GenerateBillEntity();
        generateBillEntity.setId(1L);

        BillEntity bill = new BillEntity();
        bill.setId(1L);
        generateBillEntity.setBillEntity(bill);

        RoomEntity room = new RoomEntity();
        room.setRoomNumber("TestID");
        generateBillEntity.setRoomEntity(room);

        UserEntity user = new UserEntity();
        user.setAdhaarNumber("AdhaarIDTest");
        user.setEmail("test@gmail.com");
        user.setName("Krishna");
        generateBillEntity.setUserEntity(user);

        BookingEntity booking = new BookingEntity();
        booking.setId(1L);
        booking.setCheckOut(LocalDate.of(2025,5,31));

        generateBillEntity.setBookingEntity(booking);
        generateBillEntity.setMonth(YearMonth.of(2025,5));
        generateBillEntity.setTotal(new BigDecimal(1000));
        generateBillEntity.setGenerateBillStatus(GenerateBillStatus.CALCULATED);
        generateBillEntity.setDeliveryStatus(DeliveryStatus.NOT_SENT);

        return generateBillEntity;
    }

    @Test
    @DisplayName("Should send bills monthly- Success")
    void sendBillsOnMonthEnd_SuccessTest() throws IOException, MessagingException {
        // Given
        GenerateBillEntity generateBillEntity = createGenerateBillEntity();
        byte[] pdfData = new byte[1];
        // Arrange
        when(generateBillRepository.findByMonthAndStatus(YearMonth.of(2025,5), DeliveryStatus.NOT_SENT)).thenReturn(List.of(generateBillEntity));
        when(rentalBillPdfGenerator.generateBillPdf(generateBillEntity)).thenReturn(pdfData);
        // Act
        emailScheduler.sendBillsOnMonthEnd();
        // Assert
        assertThat(generateBillEntity.getDeliveryStatus()).isEqualTo(DeliveryStatus.SENT);
        // Verify
        verify(rentalBillPdfGenerator).generateBillPdf(generateBillEntity);
        verify(emailService).sendBillWithPdf(eq("test@gmail.com"),anyString(),contains("Krishna"),eq(pdfData));
        verify(generateBillRepository).save(generateBillEntity);
    }

    @Test
    @DisplayName("Send bills monthly- No Bills Found")
    void sendBillsOnMonthEnd_NoBillsFoundTest() {
        // Arrange
        when(generateBillRepository.findByMonthAndStatus(YearMonth.of(2025,5), DeliveryStatus.NOT_SENT)).thenReturn(List.of());
        // Act
        emailScheduler.sendBillsOnMonthEnd();
        // Assert and Verify
        verifyNoInteractions(rentalBillPdfGenerator);
        verifyNoInteractions(emailService);
        verify(generateBillRepository,never()).save(any());
    }

    @Test
    @DisplayName("Send bills monthly - Exception Handling")
    void sendBillsOnMonthEnd_ExceptionTest() throws IOException, MessagingException {
        GenerateBillEntity bill1 = createGenerateBillEntity();
        GenerateBillEntity bill2 = createGenerateBillEntity();
        byte[] pdfData = new byte[]{4,56,8};
        // Arrange
        when(generateBillRepository.findByMonthAndStatus(YearMonth.of(2025,5), DeliveryStatus.NOT_SENT)).thenReturn(List.of(bill1,bill2));
        when(rentalBillPdfGenerator.generateBillPdf(bill1)).thenThrow(new IOException("PDF Error"));
        when(rentalBillPdfGenerator.generateBillPdf(bill2)).thenReturn(pdfData);
        // Act
        emailScheduler.sendBillsOnMonthEnd();
        // Assert and Verify
        assertThat(bill2.getDeliveryStatus()).isEqualTo(DeliveryStatus.SENT);

        verify(emailService,times(1)).sendBillWithPdf(anyString(),anyString(),anyString(),eq(pdfData));
        verify(generateBillRepository,times(1)).save(bill2);
    }

    @Test
    @DisplayName("Send Bills on Check Out - Success")
    void sendBillsOnCheckOut_SuccessTest() throws IOException, MessagingException {
        // Given
        GenerateBillEntity bill = createGenerateBillEntity();
        byte[] pdfData = new byte[]{4,56,8};
        // Arrange
        when(generateBillRepository.findByCheckOutDate(fixedDate,DeliveryStatus.NOT_SENT)).thenReturn(List.of(bill));
        when(rentalBillPdfGenerator.generateBillPdf(bill)).thenReturn(pdfData);
        // Act
        emailScheduler.sendBillsOnCheckOut();
        // Assert
        assertThat(bill.getDeliveryStatus()).isEqualTo(DeliveryStatus.SENT);
        // Verify
        verify(rentalBillPdfGenerator).generateBillPdf(bill);
        verify(emailService).sendBillWithPdf(eq("test@gmail.com"),anyString(),contains("Krishna"),eq(pdfData));
        verify(generateBillRepository).save(bill);
    }

    @Test
    @DisplayName("Send Bills on Check Out - No Bills Found")
    void sendBillsOnCheckOut_NoBillsFoundTest() {
        // Arrange
        when(generateBillRepository.findByCheckOutDate(fixedDate,DeliveryStatus.NOT_SENT)).thenReturn(List.of());
        // Act
        emailScheduler.sendBillsOnCheckOut();
        // Assert and Verify
        verifyNoInteractions(rentalBillPdfGenerator);
        verifyNoInteractions(emailService);
        verify(generateBillRepository,never()).save(any());
    }

    @Test
    @DisplayName("Send Bills on Check Out - Exception Handling")
    void sendBillsOnCheckOut_ExceptionTest() throws IOException, MessagingException {
        GenerateBillEntity bill1 = createGenerateBillEntity();
        GenerateBillEntity bill2 = createGenerateBillEntity();
        byte[] pdfData = new byte[]{4,56,8};
        // Arrange
        when(generateBillRepository.findByCheckOutDate(fixedDate,DeliveryStatus.NOT_SENT)).thenReturn(List.of(bill1,bill2));
        when(rentalBillPdfGenerator.generateBillPdf(bill1)).thenThrow(new IOException("PDF Error"));
        when(rentalBillPdfGenerator.generateBillPdf(bill2)).thenReturn(pdfData);
        // Act
        emailScheduler.sendBillsOnCheckOut();
        // Assert and Verify
        assertThat(bill2.getDeliveryStatus()).isEqualTo(DeliveryStatus.SENT);

        verify(emailService,times(1)).sendBillWithPdf(anyString(),anyString(),anyString(),eq(pdfData));
        verify(generateBillRepository,times(1)).save(bill2);
    }
}
