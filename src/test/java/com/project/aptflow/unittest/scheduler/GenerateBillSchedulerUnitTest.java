package com.project.aptflow.unittest.scheduler;

import com.project.aptflow.dto.BillDTO;
import com.project.aptflow.dto.billing.GenerateBillDTO;
import com.project.aptflow.dto.billing.RoomSummaryDTO;
import com.project.aptflow.dto.billing.UserSummaryDTO;
import com.project.aptflow.dto.booking.BookingDTO;
import com.project.aptflow.entity.*;
import com.project.aptflow.enums.BookingStatus;
import com.project.aptflow.mapper.BillMapper;
import com.project.aptflow.mapper.BookingMapper;
import com.project.aptflow.mapper.billing.GenerateBillMapper;
import com.project.aptflow.mapper.billing.RoomSummaryMapper;
import com.project.aptflow.mapper.billing.UserSummaryMapper;
import com.project.aptflow.repository.BookingRepository;
import com.project.aptflow.repository.GenerateBillRepository;
import com.project.aptflow.scheduler.GenerateBillScheduler;
import com.project.aptflow.service.GenerateBillService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Generate Bill Scheduler Unit Test")
public class GenerateBillSchedulerUnitTest {
    @Mock
    private Clock clock;
    @Mock
    private GenerateBillService generateBillService;
    @Mock
    private GenerateBillRepository generateBillRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private GenerateBillMapper generateBillMapper;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private RoomSummaryMapper roomSummaryMapper;
    @Mock
    private UserSummaryMapper userSummaryMapper;
    @Mock
    private BillMapper billMapper;

    @InjectMocks
    private GenerateBillScheduler generateBillScheduler;

    private final LocalDate fixedDate = LocalDate.of(2025,6,20);
    private final ZoneId zoneId = ZoneId.systemDefault();

    @BeforeEach
    void setup() {
        Clock fixedClock = Clock.fixed(fixedDate.atStartOfDay(zoneId).toInstant(),zoneId);
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(zoneId);
    }

    private BookingEntity createBookingEntity(LocalDate checkIn, LocalDate checkOut) {
        BookingEntity booking = new BookingEntity();
        booking.setCheckIn(checkIn);
        booking.setCheckOut(checkOut);
        booking.setRoomEntity(new RoomEntity());
        booking.setBillEntity(new BillEntity());
        booking.setUserEntity(new UserEntity());
        return booking;
    }

    private void mockMappers() {
        when(bookingMapper.entityToDTO(any())).thenReturn(new BookingDTO());
        when(roomSummaryMapper.entityToDTO(any())).thenReturn(new RoomSummaryDTO());
        when(userSummaryMapper.entityToDTO(any())).thenReturn(new UserSummaryDTO());
        when(billMapper.entityToDTO(any())).thenReturn(new BillDTO());
    }

    @Nested
    @DisplayName("Generate Bill For Active Booking")
    class GenerateBillForActiveBooking {
        /*
        Check in and check out dates are in same month
        Check In:2025-06-01, Check Out: 2025-06-30
        */
        @Test
        @DisplayName("Generate Bill for Active Booking Success")
        void generateBillForUser_Success() {
            BookingEntity booking = createBookingEntity(LocalDate.of(2025,6,1),LocalDate.of(2025,6,30));

            when(bookingRepository.findByState(BookingStatus.ACTIVE)).thenReturn(List.of(booking));
            mockMappers();
            // Act
            generateBillScheduler.generateBillForUser();
            // Verify
            verify(generateBillService,times(1)).generateBill(any(GenerateBillDTO.class));
        }

        /*
        Booking overlaps with current month
        Check In:2025-05-31, Check Out: 2025-06-01
        Check In:2025-06-30, Check Out: 2025-07-01
         */
        @Test
        @DisplayName("Generate Bill For Partial Overlaps")
        void generateBillForUser_PartialOverlaps() {
            BookingEntity booking1 = createBookingEntity(LocalDate.of(2025,5,31),LocalDate.of(2025,6,1));
            BookingEntity booking2 = createBookingEntity(LocalDate.of(2025,6,30),LocalDate.of(2025,7,1));

            when(bookingRepository.findByState(BookingStatus.ACTIVE)).thenReturn(List.of(booking1,booking2));
            mockMappers();
            // Act
            generateBillScheduler.generateBillForUser();
            // Verify
            verify(bookingRepository,times(1)).findByState(BookingStatus.ACTIVE);
            verify(generateBillService,times(2)).generateBill(any(GenerateBillDTO.class));
        }

        /*
        Booking is entirely in future month
        Check In:2025-07-01, Check Out: 2025-07-30
         */
        @Test
        @DisplayName("Should skip bill generation for booking in future month")
        void generateBillForUser_FutureMonth() {
            BookingEntity booking = createBookingEntity(LocalDate.of(2025,7,1),LocalDate.of(2025,7,30));
            when(bookingRepository.findByState(BookingStatus.ACTIVE)).thenReturn(List.of(booking));
            // Act
            generateBillScheduler.generateBillForUser();
            // Verify
            verify(bookingRepository,times(1)).findByState(BookingStatus.ACTIVE);
            verify(generateBillService,never()).generateBill(any(GenerateBillDTO.class));
        }

        /*
        No active booking
         */
        @Test
        @DisplayName("Should not generate Bills if there is no active bookings")
        void generateBillForUser_NoActiveBooking() {
            when(bookingRepository.findByState(BookingStatus.ACTIVE)).thenReturn(List.of());
            // Act
            generateBillScheduler.generateBillForUser();
            // Verify
            verify(bookingRepository,times(1)).findByState(BookingStatus.ACTIVE);
            verify(generateBillService,never()).generateBill(any(GenerateBillDTO.class));
        }

        /*
        Mixed active and inactive booking
        Check In:2025-07-01, Check Out: 2025-07-30
        Check In:2025-06-01, Check Out: 2025-06-30
         */
        @Test
        @DisplayName("Should skip the bill generation if booking is in future month")
        void generateBillForUser_MixedBookings() {
            BookingEntity booking1 = createBookingEntity(LocalDate.of(2025,7,1),LocalDate.of(2025,7,30));
            BookingEntity booking2 = createBookingEntity(LocalDate.of(2025,6,1),LocalDate.of(2025,6,30));
            // Arrange
            when(bookingRepository.findByState(BookingStatus.ACTIVE)).thenReturn(List.of(booking1,booking2));
            mockMappers();
            // Act
            generateBillScheduler.generateBillForUser();
            // Verify
            verify(bookingRepository,times(1)).findByState(BookingStatus.ACTIVE);
            verify(generateBillService,times(1)).generateBill(any(GenerateBillDTO.class));
        }

        /*
        Booking has null check in or check out
         */
        @Test
        @DisplayName("Should skip the bill generation if booking has null check in or check out")
        void generateBillForUser_NullCheckInOrCheckOut() {
            BookingEntity booking = createBookingEntity(null,null);
            when(bookingRepository.findByState(BookingStatus.ACTIVE)).thenReturn(List.of(booking));
            // Act
            generateBillScheduler.generateBillForUser();
            // Verify
            verify(bookingRepository,times(1)).findByState(BookingStatus.ACTIVE);
            verify(generateBillService,never()).generateBill(any(GenerateBillDTO.class));
        }
    }

    @Nested
    @DisplayName("Generate Bill On Check Out Day Test")
    class GenerateBillOnCheckOutDayTest{
        /*
        Check out date is today
         */
        @Test
        @DisplayName("Generate Bill On Check Out Day")
        void generateBillForCheckOut_CheckOutDateIsToday() {
            // Given
            BookingEntity booking = createBookingEntity(LocalDate.of(2025,6,1),LocalDate.of(2025,6,20));
            mockMappers();
            // Arrange
            when(bookingRepository.findByCheckOut(fixedDate)).thenReturn(List.of(booking));
            when(generateBillService.checkOutBill(any())).thenReturn(new BigDecimal(1000));
            when(generateBillMapper.dtoToEntity(any())).thenReturn(new GenerateBillEntity());
            // Act
            generateBillScheduler.generateBillForCheckOut();
            // Verify
            verify(bookingRepository,times(1)).findByCheckOut(fixedDate);
            verify(generateBillService,times(1)).checkOutBill(any());
            verify(generateBillRepository,times(1)).save(any());
            verify(generateBillMapper,times(1)).dtoToEntity(any());
        }

        /*
        Multiple check out bookings on today
         */
        @Test
        @DisplayName("Generate Bill for multiple check out bookings on today")
        void generateBillForCheckOut_MultipleCheckOutBookings() {
            // Given
            BookingEntity booking1 = createBookingEntity(LocalDate.of(2025,6,1),LocalDate.of(2025,6,20));
            BookingEntity booking2 = createBookingEntity(LocalDate.of(2025,5,1),LocalDate.of(2025,6,20));
            mockMappers();
            // Arrange
            when(bookingRepository.findByCheckOut(fixedDate)).thenReturn(List.of(booking1,booking2));
            when(generateBillService.checkOutBill(any())).thenReturn(new BigDecimal(1000));
            when(generateBillMapper.dtoToEntity(any())).thenReturn(new GenerateBillEntity());
            // Act
            generateBillScheduler.generateBillForCheckOut();
            // Verify
            verify(bookingRepository,times(1)).findByCheckOut(fixedDate);
            verify(generateBillService,times(2)).checkOutBill(any());
            verify(generateBillRepository,times(2)).save(any());
            verify(generateBillMapper,times(2)).dtoToEntity(any());
        }

        /*
        No check out booking on today
         */
        @Test
        @DisplayName("Generate Bill for no check out booking on today")
        void generateBillForCheckOut_NoCheckOutBookings() {
            // Arrange
            when(bookingRepository.findByCheckOut(fixedDate)).thenReturn(List.of());
            // Act
            generateBillScheduler.generateBillForCheckOut();
            // Verify
            verify(bookingRepository,times(1)).findByCheckOut(fixedDate);
            verifyNoInteractions(generateBillService);  // Instead you can use verify(generateBillService, never()).checkOutBill(any());
            verifyNoInteractions(generateBillRepository);   // Instead you can use verify(generateBillRepository, never()).save(any());
            verifyNoInteractions(generateBillMapper);   // Instead you can use verify(generateBillMapper, never()).dtoToEntity(any());
        }
    }
}
