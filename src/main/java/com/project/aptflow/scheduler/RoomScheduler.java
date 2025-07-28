package com.project.aptflow.scheduler;

import com.project.aptflow.entity.BookingEntity;
import com.project.aptflow.entity.RoomEntity;
import com.project.aptflow.enums.RoomStatus;
import com.project.aptflow.repository.BookingRepository;
import com.project.aptflow.repository.RoomRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Component
public class RoomScheduler {
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final Clock clock;

    public RoomScheduler(BookingRepository bookingRepository, RoomRepository roomRepository, Clock clock) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.clock = clock;
    }

    @Scheduled(cron = "0 0 17 * * ?")
    @Transactional
    public void updateRoomStatusBasedOnCheckOut(){
        LocalDate today = LocalDate.now(clock);
        List<BookingEntity> bookingEntityList = bookingRepository.findByCheckOut(today);
        for (BookingEntity booking : bookingEntityList){
            RoomEntity room = booking.getRoomEntity();
            if (room!=null){
                room.setRoomStatus(RoomStatus.AVAILABLE);
                roomRepository.save(room);
            }
        }
    }
}
