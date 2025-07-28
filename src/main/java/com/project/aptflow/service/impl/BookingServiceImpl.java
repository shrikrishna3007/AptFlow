package com.project.aptflow.service.impl;

import com.project.aptflow.config.auth.LoggedInUserService;
import com.project.aptflow.dto.BillDTO;
import com.project.aptflow.dto.booking.BookingDTO;
import com.project.aptflow.dto.RoomDTO;
import com.project.aptflow.dto.UserDTO;
import com.project.aptflow.entity.BillEntity;
import com.project.aptflow.entity.BookingEntity;
import com.project.aptflow.entity.RoomEntity;
import com.project.aptflow.entity.UserEntity;
import com.project.aptflow.enums.BookingStatus;
import com.project.aptflow.enums.RoomStatus;
import com.project.aptflow.exceptions.BadRequestException;
import com.project.aptflow.exceptions.ResourceNotFoundException;
import com.project.aptflow.mapper.BookingMapper;
import com.project.aptflow.repository.BillRepository;
import com.project.aptflow.repository.BookingRepository;
import com.project.aptflow.repository.RoomRepository;
import com.project.aptflow.repository.UserRepository;
import com.project.aptflow.service.BookingService;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final BillRepository billRepository;
    private final UserRepository userRepository;
    private final LoggedInUserService loggedInUserService;
    private final BookingMapper bookingMapper;

    public BookingServiceImpl(BookingRepository bookingRepository, RoomRepository roomRepository, BillRepository billRepository, UserRepository userRepository, LoggedInUserService loggedInUserService, BookingMapper bookingMapper) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.billRepository = billRepository;
        this.userRepository = userRepository;
        this.loggedInUserService = loggedInUserService;
        this.bookingMapper = bookingMapper;
    }

    @Override
    @Transactional
    public void roomBook(BookingDTO booking) {
        // validate booking dates before proceeding further
        validateBookingDates(booking.getCheckIn(),booking.getCheckOut());
        // Get room number from dto
        String roomNumber= booking.getRoomDTO().getRoomNumber();
        // check if room exists
        RoomEntity room = roomRepository.findById(roomNumber)
                .orElseThrow(()->new ResourceNotFoundException("Data not found..."));
        // check if room available or not
        if (room.getRoomStatus() == RoomStatus.OCCUPIED){
            throw new ResourceNotFoundException("Room not available...");
        }

        // Set booking status
        setBookingTerm(booking);    
        // Set the room entity in Booking Entity
        BookingEntity bookingEntity = bookingMapper.dtoToEntity(booking);
        bookingEntity.setRoomEntity(room);
        bookingRepository.save(bookingEntity);
        // update room status in room entity.
        room.setRoomStatus(RoomStatus.OCCUPIED);
        roomRepository.save(room);
    }

    /*
    This method to validate booking dates that is check in and check out.
     */
    private void validateBookingDates(LocalDate checkIn, LocalDate checkOut){
        LocalDate today = LocalDate.now();
        // check in must be today or in future
        if (checkIn.isBefore(today)){
            throw new BadRequestException("Check in date can not be previous day...");
        }
        if (checkOut == null){
            throw new BadRequestException("Check Out date is required...");
        }
        // check out must be after check in
        if (!checkOut.isAfter(checkIn)){
            throw new BadRequestException("Check Out date must be after check in date...");
        }
    }

    @Override
    public List<BookingDTO> getAllBookings() {
        List<BookingEntity> bookingEntities = bookingRepository.findAll();
        List<BookingDTO> bookingDTOS = new ArrayList<>();
        for (BookingEntity booking: bookingEntities){
            bookingDTOS.add(bookingMapper.entityToDTO(booking));
        }
        return bookingDTOS;
    }

    @Override
    @Transactional
    public BookingDTO updateBooking(Long id, BookingDTO updatedBooking) {
        BookingEntity booking = bookingRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException(" Booking details not found..."));
        bookingMapper.updateBooking(booking, updatedBooking);
        /*
        Set the existing room, user and bill before saving updated bill.
        Mapstruct does not automatically map the values like room number, user adhaar number and bill id in booking entity as Foreign Keys.
        This is used to main data consistency
         */
        updateRoom(booking,updatedBooking.getRoomDTO());
        updateBill(booking,updatedBooking.getBillDTO());
        updateUser(booking,updatedBooking.getUserDTO());

        bookingRepository.save(booking);
        return bookingMapper.entityToDTO(booking);
    }

    @Override
    public BookingDTO getBookingById(Long id) {
        BookingEntity booking = bookingRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException(" Data not found..."));
        return bookingMapper.entityToDTO(booking);
    }

    @Override
    public void deleteBooking(Long id) {
        if (!bookingRepository.existsById(id)){
            throw new ResourceNotFoundException(" Data not found...");
        }
        bookingRepository.deleteById(id);
    }

    @Override
    public void cancelBooking(Long id) {
        BookingEntity booking = bookingRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException(" Booking details not found"));

        if (!booking.isCancelled()){
            booking.setCancelled(true); // Booking is cancelled
            /*
            Once booking cancelled then it should be available to generate bill
             */
            booking.setState(BookingStatus.INACTIVE); // No longer active for billing
            bookingRepository.save(booking);
        }
    }

    @Override
    public void setBookingTerm(BookingDTO bookingDTO) {
        LocalDate today = LocalDate.now();
        if (bookingDTO.getCheckOut().isAfter(today) || bookingDTO.getCheckOut().equals(today)) {
            bookingDTO.setState(BookingStatus.ACTIVE);
        }else {
            bookingDTO.setState(BookingStatus.INACTIVE);
        }
    }

    @Override
    public BookingDTO updateCheckOutDate(Long id, LocalDate checkOut) {
        // get logged in user
        String currentUserEmail = loggedInUserService.getCurrentUserEmail();

        //get booking details
        BookingEntity booking = bookingRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException(" Booking details not found..."));

        // Validate: booking must be belonged to current user
        if (!booking.getUserEntity().getEmail().equals(currentUserEmail)){
            throw new AccessDeniedException("You are not allowed to update this booking...");
        }

        // Validate: new check out date must be after check in date
        if (checkOut.isBefore(booking.getCheckIn()) || checkOut.isEqual(booking.getCheckIn())) {
            throw new BadRequestException("Check Out date must be after check in date...");
        }

        // Validate: new check out date must not be in past
        if (checkOut.isBefore(LocalDate.now())){
            throw new BadRequestException("Check Out date can not be in past...");
        }

        // update check out
        booking.setCheckOut(checkOut);

        // Update the status
        booking.setState(BookingStatus.ACTIVE);

        // Save the object and return the dto object.
        BookingEntity updatedBooking = bookingRepository.save(booking);
        return bookingMapper.entityToDTO(updatedBooking);
    }

    private void updateUser(BookingEntity booking, UserDTO userDTO) {
        if (userDTO !=null){
            UserEntity user = userRepository.findById(userDTO.getAdhaarNumber())
                    .orElseThrow(()->new ResourceNotFoundException(" User details not found..."));
            booking.setUserEntity(user);
        }
    }

    private void updateBill(BookingEntity booking, BillDTO billDTO) {
        if (billDTO!=null){
            BillEntity bill = billRepository.findById(billDTO.getId())
                    .orElseThrow(()->new ResourceNotFoundException(" Bill details not found..."));
            booking.setBillEntity(bill);
        }
    }

    private void updateRoom(BookingEntity booking, RoomDTO roomDTO) {
        if (roomDTO!=null){
            RoomEntity room = roomRepository.findById(roomDTO.getRoomNumber())
                    .orElseThrow(()->new ResourceNotFoundException(" Room details not found..."));
            booking.setRoomEntity(room);
        }
    }
}
