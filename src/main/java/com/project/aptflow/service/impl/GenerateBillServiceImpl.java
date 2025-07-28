package com.project.aptflow.service.impl;

import com.project.aptflow.dto.billing.GenerateBillDTO;
import com.project.aptflow.entity.GenerateBillEntity;
import com.project.aptflow.enums.DeliveryStatus;
import com.project.aptflow.enums.GenerateBillStatus;
import com.project.aptflow.exceptions.ResourceNotFoundException;
import com.project.aptflow.mapper.billing.GenerateBillMapper;
import com.project.aptflow.repository.BookingRepository;
import com.project.aptflow.repository.GenerateBillRepository;
import com.project.aptflow.service.GenerateBillService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GenerateBillServiceImpl implements GenerateBillService {
    private final GenerateBillRepository generateBillRepository;
    private final BookingRepository bookingRepository;
    private final GenerateBillMapper generateBillMapper;
    private final Clock clock;

    public GenerateBillServiceImpl(GenerateBillRepository generateBillRepository, BookingRepository bookingRepository, GenerateBillMapper generateBillMapper, Clock clock) {
        this.generateBillRepository = generateBillRepository;
        this.bookingRepository = bookingRepository;
        this.generateBillMapper = generateBillMapper;
        this.clock = clock;
    }

    @Override
    public List<GenerateBillDTO> getAllBills() {
        List<GenerateBillEntity> billEntities = generateBillRepository.findAll();
        List<GenerateBillDTO> billDTOS = new ArrayList<>();
        for (GenerateBillEntity bill: billEntities){
            billDTOS.add(generateBillMapper.entityToDTO(bill));
        }
        return billDTOS;
    }

    @Override
    public GenerateBillDTO getBillById(Long id) {
        GenerateBillEntity bill = generateBillRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException(" Bill not found..."));
        return generateBillMapper.entityToDTO(bill);
    }

    @Override
    public List<GenerateBillDTO> getBillsByAdhaarNumber(String adhaarNumber) {
        List<GenerateBillEntity> billEntities = generateBillRepository.findBillsByAdhaarNumber(adhaarNumber);
        return billEntities.stream()
                .map(generateBillMapper::entityToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public GenerateBillDTO updateBill(Long id, GenerateBillDTO updateBillDTO) {
        GenerateBillEntity bill = generateBillRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException(" Bill not found..."));
        generateBillMapper.updateBillEntity(bill,updateBillDTO);
        GenerateBillEntity updatedBill = generateBillRepository.save(bill);
        return generateBillMapper.entityToDTO(updatedBill);
    }

    @Override
    public void deleteBill(Long id) {
        if (!generateBillRepository.existsById(id)){
            throw new ResourceNotFoundException(" Bill not found...");
        }
        generateBillRepository.deleteById(id);
    }

    private BigDecimal calculateElectricityBill(GenerateBillDTO generateBillDTO) {
        BigDecimal unitConsumed = generateBillDTO.getBillDTO().getElectricityBill();
        BigDecimal unitPrice = generateBillDTO.getBillDTO().getUnitPrice();
        return unitConsumed.multiply(unitPrice);
    }

    @Override
    public void generateBill(GenerateBillDTO generateBillDTO) {
        LocalDate currentDate = LocalDate.now(clock);
        YearMonth currentMonth = YearMonth.from(currentDate);
        LocalDate checkIn = generateBillDTO.getBookingDTO().getCheckIn();
        LocalDate checkOut = generateBillDTO.getBookingDTO().getCheckOut();
        long days= ChronoUnit.DAYS.between(checkIn,checkOut) + 1;

        if (days >= 30){
            YearMonth checkInMonth = YearMonth.from(checkIn);
            YearMonth checkOutMonth = YearMonth.from(checkOut);
            BigDecimal total = BigDecimal.ZERO;

            if (currentMonth.equals(checkInMonth)){
                total = total.add(partialCheckInMonthTotal(generateBillDTO));
            } else if (currentMonth.isAfter(checkInMonth) && currentMonth.isBefore(checkOutMonth)) {
                total = total.add(calculateFullMonth(generateBillDTO));
            }

            // update the original generateBillDTO rather than creating a new one
            generateBillDTO.setMonth(currentMonth);
            generateBillDTO.setTotal(total);
            generateBillDTO.setDeliveryStatus(DeliveryStatus.NOT_SENT);
            generateBillDTO.setGenerateBillStatus(GenerateBillStatus.CALCULATED);
            // convert it and save the entity
            GenerateBillEntity generateBill = generateBillMapper.dtoToEntity(generateBillDTO);
            generateBillRepository.save(generateBill);
        }
    }

    private BigDecimal calculateFullMonth(GenerateBillDTO generateBillDTO) {
        BigDecimal electricityBill = calculateElectricityBill(generateBillDTO);
        BigDecimal roomRent = generateBillDTO.getRoomSummaryDTO().getRoomRent();
        return roomRent.add(electricityBill);
    }

    private BigDecimal partialCheckInMonthTotal(GenerateBillDTO generateBillDTO) {
        LocalDate checkIn = generateBillDTO.getBookingDTO().getCheckIn();
        int checkInDay = checkIn.getDayOfMonth();
        int lastDayOfMonth = checkIn.lengthOfMonth();
        int remainingDays = checkIn.lengthOfMonth() - checkInDay + 1;
        BigDecimal rentPerMonth = generateBillDTO.getRoomSummaryDTO().getRoomRent();
        BigDecimal electricity = calculateElectricityBill(generateBillDTO);
        if(checkInDay == 1){
            return rentPerMonth.add(electricity);
        } else {
            BigDecimal partialRent = rentPerMonth
                    .multiply(BigDecimal.valueOf(remainingDays))
                    .divide(BigDecimal.valueOf(lastDayOfMonth), 2, RoundingMode.HALF_UP);
            return partialRent.add(electricity);
        }
    }

    /*
    Method for calculating bill based on check out.
     */
    @Override
    public BigDecimal checkOutBill(GenerateBillDTO generateBillDTO) {
        LocalDate checkOut = generateBillDTO.getBookingDTO().getCheckOut();
        LocalDate checkIn = generateBillDTO.getBookingDTO().getCheckIn();
        int daysTillCheckOut = checkOut.getDayOfMonth();
        int lastDayOfMonth = checkOut.lengthOfMonth();
        BigDecimal rentPerMonth = generateBillDTO.getRoomSummaryDTO().getRoomRent();
        BigDecimal electricityBill = calculateElectricityBill(generateBillDTO);
        long days = ChronoUnit.DAYS.between(checkIn,checkOut) + 1;

        BigDecimal partialMonthRent = rentPerMonth.multiply(BigDecimal.valueOf(daysTillCheckOut))
                .divide(BigDecimal.valueOf(lastDayOfMonth),2,RoundingMode.HALF_UP);
        /*
        If user staying less than month then calculate partial month. This is for when user staying less than 30 days.
         */
        if (days < 30){
            return partialMonthRent.add(electricityBill);
        }
        /*
        When check out date at the end of the month then it considered as a month.
         */
        if (daysTillCheckOut == lastDayOfMonth){
            return rentPerMonth.add(electricityBill);
        }
        /*
        This is for when user stays longer than 30 days. But check out in between months.
         */
        return partialMonthRent.add(electricityBill);
    }
}
