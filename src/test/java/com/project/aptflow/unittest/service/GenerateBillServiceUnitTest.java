package com.project.aptflow.unittest.service;

import com.project.aptflow.dto.BillDTO;
import com.project.aptflow.dto.billing.GenerateBillDTO;
import com.project.aptflow.dto.billing.RoomSummaryDTO;
import com.project.aptflow.dto.billing.UserSummaryDTO;
import com.project.aptflow.dto.booking.BookingDTO;
import com.project.aptflow.entity.GenerateBillEntity;
import com.project.aptflow.enums.BookingStatus;
import com.project.aptflow.enums.DeliveryStatus;
import com.project.aptflow.enums.GenerateBillStatus;
import com.project.aptflow.enums.RoomStatus;
import com.project.aptflow.exceptions.ResourceNotFoundException;
import com.project.aptflow.mapper.billing.GenerateBillMapper;
import com.project.aptflow.repository.BookingRepository;
import com.project.aptflow.repository.GenerateBillRepository;
import com.project.aptflow.service.impl.GenerateBillServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Generate Bill Service Unit Tests")
public class GenerateBillServiceUnitTest {
    @Mock
    private GenerateBillRepository generateBillRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private GenerateBillMapper generateBillMapper;

    @InjectMocks
    private GenerateBillServiceImpl generateBillServiceImpl;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        Clock fixedClock = Clock.fixed(LocalDate.of(2025, 6, 1).atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        generateBillServiceImpl = new GenerateBillServiceImpl(generateBillRepository, bookingRepository, generateBillMapper, fixedClock);
    }

    // Reusable DTO and Entity Builders
    private GenerateBillDTO buildGenerateBillDTO() {
        RoomSummaryDTO roomSummaryDTO = new RoomSummaryDTO();
        roomSummaryDTO.setRoomNumber("RoomID-Test");
        roomSummaryDTO.setRoomCapacity(2);
        roomSummaryDTO.setRoomSharing(true);
        roomSummaryDTO.setRoomStatus(RoomStatus.OCCUPIED);
        roomSummaryDTO.setRoomRent(new BigDecimal(1000));

        UserSummaryDTO userSummaryDTO = new UserSummaryDTO();
        userSummaryDTO.setAdhaarNumber("TestID");
        userSummaryDTO.setName("TestName");
        userSummaryDTO.setEmail("TestEmail");

        BillDTO billDTO = new BillDTO();
        billDTO.setId(1L);
        billDTO.setRoomNumber(roomSummaryDTO.getRoomNumber());
        billDTO.setMonth("2025-06");
        billDTO.setElectricityBill(new BigDecimal(1000));
        billDTO.setUnitPrice(new BigDecimal(2));
        billDTO.setRentPerDay(new BigDecimal(500));

        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setId(1L);
        bookingDTO.setCheckIn(LocalDate.now());
        bookingDTO.setCheckOut(LocalDate.now().plusDays(1));
        bookingDTO.setBillDTO(billDTO);
        bookingDTO.setState(BookingStatus.ACTIVE);
        bookingDTO.setCancelled(false);

        GenerateBillDTO generateBillDTO = new GenerateBillDTO();
        generateBillDTO.setId(1L);
        generateBillDTO.setRoomSummaryDTO(roomSummaryDTO);
        generateBillDTO.setUserSummaryDTO(userSummaryDTO);
        generateBillDTO.setBillDTO(billDTO);
        generateBillDTO.setBookingDTO(bookingDTO);

        return generateBillDTO;
    }

    private GenerateBillEntity buildGenerateBillEntity() {
        GenerateBillEntity generateBillEntity = new GenerateBillEntity();
        generateBillEntity.setId(1L);
        generateBillEntity.setMonth(YearMonth.of(2025, 6));
        generateBillEntity.setTotal(new BigDecimal(1000));
        generateBillEntity.setDeliveryStatus(DeliveryStatus.NOT_SENT);
        generateBillEntity.setGenerateBillStatus(GenerateBillStatus.CALCULATED);
        return generateBillEntity;
    }

    @Nested
    @DisplayName("Get Bills Tests")
    class GetBillsTests {
        @Test
        @DisplayName("Get All Bills")
        void getAllBills_SuccessTest() {
            List<GenerateBillEntity> billEntities = List.of(buildGenerateBillEntity());
            // Arrange
            when(generateBillRepository.findAll()).thenReturn(billEntities);
            when(generateBillMapper.entityToDTO(any())).thenReturn(buildGenerateBillDTO());
            // Act
            List<GenerateBillDTO> result = generateBillServiceImpl.getAllBills();
            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(1L);

            // Verify
            verify(generateBillRepository, times(1)).findAll();
            verify(generateBillMapper, times(1)).entityToDTO(any());
        }

        @Test
        @DisplayName("Get All Bills Empty List")
        void getAllBills_EmptyListTest() {
            List<GenerateBillEntity> billEntities = List.of();
            List<GenerateBillDTO> billDTOs = List.of();
            // Arrange
            when(generateBillRepository.findAll()).thenReturn(billEntities);
            // Act
            List<GenerateBillDTO> result = generateBillServiceImpl.getAllBills();
            // Assert
            assertThat(result).hasSize(0);
            assertThat(result).isEqualTo(billDTOs);
            // Verify
            verify(generateBillRepository, times(1)).findAll();
            verifyNoMoreInteractions(generateBillRepository);
        }

        @Test
        @DisplayName("Get Bill By Id Success")
        void getBillById_SuccessTest() {
            // Arrange
            when(generateBillRepository.findById(1L)).thenReturn(Optional.of(buildGenerateBillEntity()));
            when(generateBillMapper.entityToDTO(any())).thenReturn(buildGenerateBillDTO());
            // Act
            GenerateBillDTO result = generateBillServiceImpl.getBillById(1L);
            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            // Verify
            verify(generateBillRepository, times(1)).findById(1L);
            verify(generateBillMapper, times(1)).entityToDTO(any());
            verifyNoMoreInteractions(generateBillRepository, generateBillMapper);
        }

        @Test
        @DisplayName("Get Bill By Id Failure")
        void getBillById_FailureTest() {
            // Arrange
            when(generateBillRepository.findById(1L)).thenReturn(Optional.empty());
            // Act & Assert
            assertThatThrownBy(() -> generateBillServiceImpl.getBillById(1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(" Bill not found...");
            // Verify
            verify(generateBillRepository, times(1)).findById(1L);
            verifyNoMoreInteractions(generateBillRepository);
        }

        @Test
        @DisplayName("Get Bills By Adhaar Number Success")
        void getBillsByAdhaarNumber_SuccessTest() {
            // Arrange
            when(generateBillRepository.findBillsByAdhaarNumber("TestID")).thenReturn(List.of(buildGenerateBillEntity()));
            when(generateBillMapper.entityToDTO(any())).thenReturn(buildGenerateBillDTO());
            // Act
            List<GenerateBillDTO> result = generateBillServiceImpl.getBillsByAdhaarNumber("TestID");
            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(1L);
            assertThat(result.get(0).getUserSummaryDTO().getAdhaarNumber()).isEqualTo("TestID");
            // Verify
            verify(generateBillRepository, times(1)).findBillsByAdhaarNumber("TestID");
            verify(generateBillMapper, times(1)).entityToDTO(any());
            verifyNoMoreInteractions(generateBillRepository, generateBillMapper);
        }

        @Test
        @DisplayName("Get Bills By Adhaar Number Returns Empty List")
        void getBillsByAdhaarNumber_EmptyListTest() {
            // Arrange
            when(generateBillRepository.findBillsByAdhaarNumber("TestID")).thenReturn(List.of());
            // Act
            List<GenerateBillDTO> result = generateBillServiceImpl.getBillsByAdhaarNumber("TestID");
            // Assert
            assertThat(result).hasSize(0);
            // Verify
            verify(generateBillRepository, times(1)).findBillsByAdhaarNumber("TestID");
            verifyNoMoreInteractions(generateBillRepository);
        }
    }

    @Nested
    @DisplayName("Update Bill Tests")
    class UpdateBillTests {
        @Test
        @DisplayName("Update Bill Success")
        void updateBill_SuccessTest() {
            // Arrange
            when(generateBillRepository.findById(1L)).thenReturn(Optional.of(buildGenerateBillEntity()));
            when(generateBillRepository.save(buildGenerateBillEntity())).thenReturn(buildGenerateBillEntity());
            when(generateBillMapper.entityToDTO(any())).thenReturn(buildGenerateBillDTO());
            // Act
            GenerateBillDTO result = generateBillServiceImpl.updateBill(1L, buildGenerateBillDTO());
            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            // Verify
            verify(generateBillRepository, times(1)).findById(1L);
            verify(generateBillMapper, times(1)).updateBillEntity(any(), any());
            verify(generateBillRepository, times(1)).save(any());
            verify(generateBillMapper, times(1)).entityToDTO(any());
            verifyNoMoreInteractions(generateBillRepository, generateBillMapper);
        }

        @Test
        @DisplayName("Update Bill Not Found")
        void updateBill_NotFoundTest() {
            // Arrange
            when(generateBillRepository.findById(1L)).thenReturn(Optional.empty());
            // Act & Assert
            assertThatThrownBy(() -> generateBillServiceImpl.updateBill(1L, buildGenerateBillDTO()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(" Bill not found...");
            // Verify
            verify(generateBillRepository, times(1)).findById(1L);
            verifyNoMoreInteractions(generateBillRepository);
        }
    }

    @Nested
    @DisplayName("Delete Bill Tests")
    class DeleteBillTests {
        @Test
        @DisplayName("Delete Bill Success")
        void deleteBill_SuccessTest() {
            // Arrange
            when(generateBillRepository.existsById(1L)).thenReturn(true);
            // Act
            generateBillServiceImpl.deleteBill(1L);
            // Verify
            verify(generateBillRepository, times(1)).existsById(1L);
            verify(generateBillRepository, times(1)).deleteById(1L);
            verifyNoMoreInteractions(generateBillRepository);
        }

        @Test
        @DisplayName("Delete Bill Not Found")
        void deleteBill_NotFoundTest() {
            // Arrange
            when(generateBillRepository.existsById(1L)).thenReturn(false);
            // Act & Assert
            assertThatThrownBy(() -> generateBillServiceImpl.deleteBill(1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(" Bill not found...");
            // Verify
            verify(generateBillRepository, times(1)).existsById(1L);
            verify(generateBillRepository,never()).deleteById(1L);
            verifyNoMoreInteractions(generateBillRepository);
        }
    }

    @Nested
    @DisplayName("Bill Generation Tests")
    class BillGenerationTests {
        @Test
        @DisplayName("Should calculate partial month rent when check-in is in current month")
        void generateBill_PartialMonthTest() {
            // Arrange
            GenerateBillDTO dto = buildGenerateBillDTO();
            dto.getBookingDTO().setCheckIn(LocalDate.of(2025, 6, 20));
            dto.getBookingDTO().setCheckOut(LocalDate.of(2025, 7, 30));
            // Call the method
            when(generateBillMapper.dtoToEntity(any())).thenReturn(buildGenerateBillEntity());
            generateBillServiceImpl.generateBill(dto);
            // Assert
            assertThat(dto.getGenerateBillStatus()).isEqualTo(GenerateBillStatus.CALCULATED);
            assertThat(dto.getDeliveryStatus()).isEqualTo(DeliveryStatus.NOT_SENT);
            assertThat(dto.getMonth()).isEqualTo(YearMonth.of(2025, 6));
            // Verify
            verify(generateBillMapper, times(1)).dtoToEntity(any());
            verify(generateBillRepository, times(1)).save(any());
            verifyNoMoreInteractions(generateBillMapper, generateBillRepository);
        }

        @Test
        @DisplayName("Should calculate full month rent if current month is between check-in and check-out")
        void generateBill_FullMonthTest() {
            GenerateBillDTO dto = buildGenerateBillDTO();
            dto.getBookingDTO().setCheckIn(LocalDate.of(2025, 5, 1));
            dto.getBookingDTO().setCheckOut(LocalDate.of(2025, 7, 1));
            // Call the method
            when(generateBillMapper.dtoToEntity(any())).thenReturn(buildGenerateBillEntity());
            generateBillServiceImpl.generateBill(dto);
            // Assert
            assertThat(dto.getGenerateBillStatus()).isEqualTo(GenerateBillStatus.CALCULATED);
            assertThat(dto.getDeliveryStatus()).isEqualTo(DeliveryStatus.NOT_SENT);
            assertThat(dto.getMonth()).isEqualTo(YearMonth.of(2025, 6));
            // Verify
            verify(generateBillMapper, times(1)).dtoToEntity(any());
            verify(generateBillRepository, times(1)).save(any());
            verifyNoMoreInteractions(generateBillMapper, generateBillRepository);
        }
    }

    @Nested
    @DisplayName("Bill Generation On Check Out Tests")
    class BillGenerationOnCheckOutTests {
        @Test
        @DisplayName("Should return partial rent + electricity bill when stay is < 30 days")
        void generateBill_checkOut_LessThan30Days() {
            GenerateBillDTO dto = buildGenerateBillDTO();
            dto.getBookingDTO().setCheckIn(LocalDate.of(2025,6,1));
            dto.getBookingDTO().setCheckOut(LocalDate.of(2025,6,27));
            // Call the method
            BigDecimal result = generateBillServiceImpl.checkOutBill(dto);
            BigDecimal expectedRent = new BigDecimal(1000)
                    .multiply(BigDecimal.valueOf(27))
                    .divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);
            BigDecimal expectedTotal = expectedRent.add(new BigDecimal(2000));
            // Assert
            assertThat(result).isEqualTo(expectedTotal);
            // Verify
            verifyNoMoreInteractions(generateBillRepository);
        }

        @Test
        @DisplayName("Should return full rent + electricity when stay>30 and checkOut is end of month")
        void generateBill_checkOut_EndOfMonth() {
            GenerateBillDTO dto = buildGenerateBillDTO();
            dto.getBookingDTO().setCheckIn(LocalDate.of(2025,6,1));
            dto.getBookingDTO().setCheckOut(LocalDate.of(2025,6,30));
            // Call the method
            BigDecimal result = generateBillServiceImpl.checkOutBill(dto);
            BigDecimal expectedRent = new BigDecimal(1000)
                    .add(new BigDecimal(2000));
            // Assert
            assertThat(result).isEqualTo(expectedRent);
            // Verify
            verifyNoMoreInteractions(generateBillRepository);
        }

        @Test
        @DisplayName("Should return partial rent + electricity when stay is > 30 days and checkOut is not end of month")
        void generateBill_checkOut_MoreThan30Days() {
            GenerateBillDTO dto = buildGenerateBillDTO();
            dto.getBookingDTO().setCheckIn(LocalDate.of(2025,6,1));
            dto.getBookingDTO().setCheckOut(LocalDate.of(2025,7,25));
            // Call the method
            BigDecimal result = generateBillServiceImpl.checkOutBill(dto);
            BigDecimal expectedRent = new BigDecimal(1000)
                    .multiply(BigDecimal.valueOf(25))
                    .divide(BigDecimal.valueOf(31), 2, RoundingMode.HALF_UP);
            BigDecimal expectedTotalRent = expectedRent.add(new BigDecimal(2000));
            // Assert
            assertThat(result).isEqualTo(expectedTotalRent);
            // Verify
            verifyNoMoreInteractions(generateBillRepository);
        }
    }
}
