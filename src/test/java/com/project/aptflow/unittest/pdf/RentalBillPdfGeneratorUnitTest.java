package com.project.aptflow.unittest.pdf;

import com.project.aptflow.entity.*;
import com.project.aptflow.enums.DeliveryStatus;
import com.project.aptflow.enums.GenerateBillStatus;
import com.project.aptflow.pdf.RentalBillPdfGenerator;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RentalBillPdfGeneratorUnitTest {
    @Mock
    private ResourceLoader resourceLoader;
    @Mock
    private Resource resource;

    @InjectMocks
    private RentalBillPdfGenerator rentalBillPdfGenerator;

    private GenerateBillEntity generateBill;

    @BeforeEach
    void setUp(){
        generateBill = createGenerateBill();
    }

    @Test
    @DisplayName("PDF Generation Success Test")
    void generateBillPdf_SuccessTest() throws Exception {
        // Arrange
        Resource realImg = new ClassPathResource("logo3.png");
        when(resourceLoader.getResource("classpath:logo3.png")).thenReturn(realImg);

        // Act
        byte[] result = rentalBillPdfGenerator.generateBillPdf(generateBill);
        // Assert
        assertThat(result).hasSizeGreaterThan(0);

        // Verify PDF content
        try (PDDocument document = PDDocument.load(result)) {
            assertThat(document.getNumberOfPages()).isEqualTo(1);

            PDFTextStripper stripper = new PDFTextStripper();
            String pdfContent = stripper.getText(document);
            assertThat(pdfContent).contains("SaiPrabha Apartment");
            assertThat(pdfContent).contains("Green Valley , Kuntaliguli,PO Mangalagangothri ,Konaje Village");
            assertThat(pdfContent).contains("Konaje Proper, Karnataka 574199");

            assertThat(pdfContent).contains("Rental Bill Details ");
            assertThat(pdfContent).contains("AdhaarIDTest");
            assertThat(pdfContent).contains("TestID");
            assertThat(pdfContent).contains(new BigDecimal("1000").toString());
        }
    }

    @Test
    @DisplayName("PDF Generation Failure Test: Resource not found")
    void generateBillPdf_FailureTest() throws IOException {
        // Arrange
        when(resourceLoader.getResource("classpath:logo3.png")).thenReturn(resource);
        when(resource.getInputStream()).thenThrow(new IOException("Resource not found"));
        // Act & Assert
        assertThatThrownBy(() -> rentalBillPdfGenerator.generateBillPdf(generateBill))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Resource not found");
    }

    @Test
    @DisplayName("PDF Generation Failure Test: Resource Is Invalid")
    void generateBillPdf_InvalidResourceTest() throws Exception {
        // Arrange
        when(resourceLoader.getResource("classpath:logo3.png")).thenReturn(resource);
        when(resource.getInputStream()).thenThrow(new IOException("Invalid resource"));

        // Act & Assert
        assertThatThrownBy(() -> rentalBillPdfGenerator.generateBillPdf(generateBill))
                .isInstanceOf(IOException.class)
                .hasMessage("Invalid resource");
        verify(resourceLoader).getResource("classpath:logo3.png");
    }

    @Test
    @DisplayName("PDF Generation Failure Test: Generate Bill Is Null")
    void generateBillPdf_NullGenerateBillTest() {
        assertThatThrownBy(()->rentalBillPdfGenerator.generateBillPdf(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("PDF Generation Success Test: Resource Contains Null Values")
    void generateBillPdf_ResourceContainsNullValuesTest() throws Exception {
        // Arrange
        Resource realImg = new ClassPathResource("logo3.png");
        when(resourceLoader.getResource("classpath:logo3.png")).thenReturn(realImg);
        GenerateBillEntity entityWithNullValues = createGenerateBillWithNullValues();
        // Act
        byte[] result = rentalBillPdfGenerator.generateBillPdf(entityWithNullValues);
        // Assert
        assertThat(result)
                .hasSizeGreaterThan(0)
                .isNotNull();

        // Verify PDF content
        try (PDDocument document = PDDocument.load(result)) {
            assertThat(document.getNumberOfPages()).isEqualTo(1);

            PDFTextStripper stripper = new PDFTextStripper();
            String pdfContent = stripper.getText(document);
            // Should contain null values
            assertThat(pdfContent).contains("null");
        }
    }

    @Test
    @DisplayName("PDF Generation Success Test: Data Contains Large Values")
    void generateBillPdf_DataContainsLargeValuesTest() throws Exception {
        // Arrange
        Resource realImg = new ClassPathResource("logo3.png");
        when(resourceLoader.getResource("classpath:logo3.png")).thenReturn(realImg);
        GenerateBillEntity entityWithLargeValues = createGenerateBillWithLargeValues();
        // Act
        byte[] result = rentalBillPdfGenerator.generateBillPdf(entityWithLargeValues);
        // Assert
        assertThat(result)
                .hasSizeGreaterThan(0)
                .isNotNull();
        // Verify PDF content
        try (PDDocument document = PDDocument.load(result)) {
            assertThat(document.getNumberOfPages()).isEqualTo(1);

            PDFTextStripper stripper = new PDFTextStripper();
            String pdfContent = stripper.getText(document);
            // Should contain large values
            assertThat(pdfContent).contains("9999999999999999999999999999999"); // Adhaar Number
            assertThat(pdfContent).contains("999999999999.99"); // Total Amount
        }
    }

    @Test
    @DisplayName("Constructor Should Create A Non-Null Generator Instance")
    void testConstructor() {
        // Act
        RentalBillPdfGenerator pdfGenerator = new RentalBillPdfGenerator(resourceLoader);
        // Assert
        assertThat(pdfGenerator).isNotNull();
    }

    private GenerateBillEntity createGenerateBill() {
        GenerateBillEntity entity = new GenerateBillEntity();
        entity.setId(1L);

        BillEntity bill = new BillEntity();
        bill.setId(1L);
        entity.setBillEntity(bill);

        RoomEntity room = new RoomEntity();
        room.setRoomNumber("TestID");
        entity.setRoomEntity(room);

        UserEntity user = new UserEntity();
        user.setAdhaarNumber("AdhaarIDTest");
        user.setEmail("test@gmail.com");
        user.setName("Krishna");
        entity.setUserEntity(user);

        BookingEntity booking = new BookingEntity();
        booking.setId(1L);
        entity.setBookingEntity(booking);

        entity.setMonth(YearMonth.of(2025,5));
        entity.setTotal(new BigDecimal(1000));
        entity.setGenerateBillStatus(GenerateBillStatus.CALCULATED);
        entity.setDeliveryStatus(DeliveryStatus.NOT_SENT);

        return entity;
    }

    private GenerateBillEntity createGenerateBillWithNullValues() {
        GenerateBillEntity entity = new GenerateBillEntity();
        entity.setId(1L);

        BillEntity bill = new BillEntity();
        bill.setId(null);
        entity.setBillEntity(bill);

        RoomEntity room = new RoomEntity();
        room.setRoomNumber("TestID");
        entity.setRoomEntity(room);

        UserEntity user = new UserEntity();
        user.setAdhaarNumber("null");
        user.setEmail("test@gmail.com");
        user.setName("Krishna");
        entity.setUserEntity(user);

        BookingEntity booking = new BookingEntity();
        booking.setId(null);
        entity.setBookingEntity(booking);

        entity.setMonth(YearMonth.of(2025,5));
        entity.setTotal(null);
        entity.setGenerateBillStatus(GenerateBillStatus.CALCULATED);
        entity.setDeliveryStatus(DeliveryStatus.NOT_SENT);

        return entity;
    }

    private GenerateBillEntity createGenerateBillWithLargeValues() {
        GenerateBillEntity entity = new GenerateBillEntity();
        entity.setId(Long.MAX_VALUE);

        BillEntity bill = new BillEntity();
        bill.setId(Long.MAX_VALUE);
        entity.setBillEntity(bill);

        RoomEntity room = new RoomEntity();
        room.setRoomNumber("TestID");
        entity.setRoomEntity(room);

        UserEntity user = new UserEntity();
        user.setAdhaarNumber("9999999999999999999999999999999");
        user.setEmail("test@gmail.com");
        user.setName("Krishna");
        entity.setUserEntity(user);

        BookingEntity booking = new BookingEntity();
        booking.setId(Long.MAX_VALUE);
        entity.setBookingEntity(booking);

        entity.setMonth(YearMonth.of(2025,5));
        entity.setTotal(new BigDecimal("999999999999.99"));
        entity.setGenerateBillStatus(GenerateBillStatus.CALCULATED);
        entity.setDeliveryStatus(DeliveryStatus.NOT_SENT);

        return entity;
    }
}
