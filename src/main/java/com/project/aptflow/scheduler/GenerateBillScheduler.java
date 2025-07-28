package com.project.aptflow.scheduler;

import com.project.aptflow.dto.billing.GenerateBillDTO;
import com.project.aptflow.entity.BookingEntity;
import com.project.aptflow.entity.GenerateBillEntity;
import com.project.aptflow.enums.BookingStatus;
import com.project.aptflow.enums.GenerateBillStatus;
import com.project.aptflow.mapper.BillMapper;
import com.project.aptflow.mapper.BookingMapper;
import com.project.aptflow.mapper.billing.GenerateBillMapper;
import com.project.aptflow.mapper.billing.RoomSummaryMapper;
import com.project.aptflow.mapper.billing.UserSummaryMapper;
import com.project.aptflow.repository.BookingRepository;
import com.project.aptflow.repository.GenerateBillRepository;
import com.project.aptflow.service.GenerateBillService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Component
public class GenerateBillScheduler {
    private final Clock clock;

    private final GenerateBillService generateBillService;
    private final GenerateBillRepository generateBillRepository;
    private final BookingRepository bookingRepository;
    private final GenerateBillMapper generateBillMapper;
    private final BookingMapper bookingMapper;
    private final RoomSummaryMapper roomSummaryMapper;
    private final UserSummaryMapper userSummaryMapper;
    private final BillMapper billMapper;

    public GenerateBillScheduler(Clock clock, GenerateBillService generateBillService, GenerateBillRepository generateBillRepository, BookingRepository bookingRepository, GenerateBillMapper generateBillMapper, BookingMapper bookingMapper, RoomSummaryMapper roomSummaryMapper, UserSummaryMapper userSummaryMapper, BillMapper billMapper) {
        this.clock = clock;
        this.generateBillService = generateBillService;
        this.generateBillRepository = generateBillRepository;
        this.bookingRepository = bookingRepository;
        this.generateBillMapper = generateBillMapper;
        this.bookingMapper = bookingMapper;
        this.roomSummaryMapper = roomSummaryMapper;
        this.userSummaryMapper = userSummaryMapper;
        this.billMapper = billMapper;
    }

    /*
    This method is used to Generate Bill for Active Bookings in Booking entity. This method will run every day at 3 AM IST.
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void generateBillForUser(){
        LocalDate today = LocalDate.now(clock);
        List<BookingEntity> bookingEntityList = bookingRepository.findByState(BookingStatus.ACTIVE);
        for (BookingEntity booking : bookingEntityList){
            LocalDate checkIn = booking.getCheckIn();
            LocalDate checkOut = booking.getCheckOut();
            if (checkIn!= null&& checkOut!= null && checkIn.isBefore(today.withDayOfMonth(1).plusMonths(1)) && checkOut.isAfter(today.withDayOfMonth(1).minusDays(1))){
                GenerateBillDTO generateBillDTO = new GenerateBillDTO();
                generateBillDTO.setBookingDTO(bookingMapper.entityToDTO(booking));
                generateBillDTO.setRoomSummaryDTO(roomSummaryMapper.entityToDTO(booking.getRoomEntity()));
                generateBillDTO.setUserSummaryDTO(userSummaryMapper.entityToDTO(booking.getUserEntity()));
                generateBillDTO.setBillDTO(billMapper.entityToDTO(booking.getBillEntity()));
                generateBillService.generateBill(generateBillDTO);
            }
        }
    }

    /*
    This method to calculate bill based on check out.
    The method will cover for following scenarios. They are,
    1. If User staying less than month.
    2. If User check out on last day of any month.
    3. If user check out in between any month.
     */
    @Scheduled(cron = "0 0 18 * * ?")
    public void generateBillForCheckOut(){
        LocalDate today = LocalDate.now(clock);
        List<BookingEntity> bookingEntityList = bookingRepository.findByCheckOut(today);
        for (BookingEntity booking : bookingEntityList){
            GenerateBillDTO generateBillDTO = new GenerateBillDTO();
            generateBillDTO.setBillDTO(billMapper.entityToDTO(booking.getBillEntity()));
            generateBillDTO.setBookingDTO(bookingMapper.entityToDTO(booking));
            generateBillDTO.setRoomSummaryDTO(roomSummaryMapper.entityToDTO(booking.getRoomEntity()));
            generateBillDTO.setUserSummaryDTO(userSummaryMapper.entityToDTO(booking.getUserEntity()));
            BigDecimal total = generateBillService.checkOutBill(generateBillDTO);
            generateBillDTO.setTotal(total);
            generateBillDTO.setMonth(YearMonth.from(booking.getCheckOut()));
            generateBillDTO.setGenerateBillStatus(GenerateBillStatus.CALCULATED);
            GenerateBillEntity generateBill = generateBillMapper.dtoToEntity(generateBillDTO);
            generateBillRepository.save(generateBill);
        }
    }
}
