package com.project.aptflow.unittest.scheduler;

import com.project.aptflow.entity.BookingEntity;
import com.project.aptflow.entity.RoomEntity;
import com.project.aptflow.enums.RoomStatus;
import com.project.aptflow.repository.BookingRepository;
import com.project.aptflow.repository.RoomRepository;
import com.project.aptflow.scheduler.RoomScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Room Scheduler Unit Test")
public class RoomSchedulerUnitTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private Clock clock;
    @InjectMocks
    private RoomScheduler roomScheduler;

    private final LocalDate fixedDate = LocalDate.of(2025,6,19);
    private final ZoneId zoneID = ZoneId.systemDefault();

    @BeforeEach
    void setup() {

        Clock fixedClock = Clock.fixed(fixedDate.atStartOfDay(zoneID).toInstant(), zoneID);
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
    }

    @Test
    @DisplayName("Update Room Status Based on Check Out")
    void updateRoomStatusBasedOnCheckOut_Success(){
        // Given
        RoomEntity room = new RoomEntity();
        room.setRoomStatus(RoomStatus.OCCUPIED);

        BookingEntity booking = new BookingEntity();
        booking.setRoomEntity(room);
        // Arrange
        when(bookingRepository.findByCheckOut(fixedDate)).thenReturn(List.of(booking));

        // Act
        roomScheduler.updateRoomStatusBasedOnCheckOut();
        // Assert
        assertThat(room.getRoomStatus()).isEqualTo(RoomStatus.AVAILABLE);
        // Verify
        verify(bookingRepository,times(1)).findByCheckOut(fixedDate);
        verify(roomRepository,times(1)).save(room);
    }

    @Test
    @DisplayName("Does not update room status when no booking found")
    void updateRoomStatusBasedOnCheckOut_NoBookingFound(){
        // Arrange
        when(bookingRepository.findByCheckOut(fixedDate)).thenReturn(List.of());
        // Act
        roomScheduler.updateRoomStatusBasedOnCheckOut();
        // Verify
        verify(bookingRepository,times(1)).findByCheckOut(fixedDate);
        verify(roomRepository,never()).save(any());
    }

    @Test
    @DisplayName("Skip update room status when room is null")
    void updateRoomStatusBasedOnCheckOut_RoomIsNull(){
        // Given
        BookingEntity booking = new BookingEntity();
        booking.setRoomEntity(null);
        // Arrange
        when(bookingRepository.findByCheckOut(fixedDate)).thenReturn(List.of(booking));
        // Act
        roomScheduler.updateRoomStatusBasedOnCheckOut();
        // Verify
        verify(bookingRepository,times(1)).findByCheckOut(fixedDate);
        verify(roomRepository,never()).save(any());
    }
}
