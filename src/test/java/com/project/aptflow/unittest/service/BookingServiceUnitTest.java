package com.project.aptflow.unittest.service;

import com.project.aptflow.config.auth.LoggedInUserService;
import com.project.aptflow.dto.BillDTO;
import com.project.aptflow.dto.RoomDTO;
import com.project.aptflow.dto.UserDTO;
import com.project.aptflow.dto.booking.BookingDTO;
import com.project.aptflow.entity.BillEntity;
import com.project.aptflow.entity.BookingEntity;
import com.project.aptflow.entity.RoomEntity;
import com.project.aptflow.entity.UserEntity;
import com.project.aptflow.enums.BookingStatus;
import com.project.aptflow.enums.Role;
import com.project.aptflow.enums.RoomStatus;
import com.project.aptflow.exceptions.BadRequestException;
import com.project.aptflow.exceptions.ResourceNotFoundException;
import com.project.aptflow.mapper.BookingMapper;
import com.project.aptflow.repository.BillRepository;
import com.project.aptflow.repository.BookingRepository;
import com.project.aptflow.repository.RoomRepository;
import com.project.aptflow.repository.UserRepository;
import com.project.aptflow.service.impl.BookingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DisplayName("Booking Service Unit Tests")
public class BookingServiceUnitTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private BillRepository billRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private LoggedInUserService loggedInUserService;

    @InjectMocks
    private BookingServiceImpl bookingServiceImpl;

    private BookingDTO bookingDTO;
    private BookingEntity bookingEntity;
    private RoomEntity roomEntity;
    private UserEntity userEntity;
    private BillEntity billEntity;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);

        // USER Data
        UserDTO userDTO = new UserDTO("Test-Number", "Name-Test", "Test-address", "Gender-Test", "Mobile-Test", "Email-Test", Role.ROLE_USER);
        userEntity = new UserEntity();
        userEntity.setAdhaarNumber("Test-Number");
        userEntity.setEmail("Email-Test");

        // ROOM Data
        RoomDTO roomDTO = new RoomDTO();
        roomDTO.setRoomNumber("TestRoomID1");
        roomEntity = new RoomEntity();
        roomEntity.setRoomNumber("TestRoomID1");
        roomEntity.setRoomStatus(RoomStatus.AVAILABLE);

        // BILL Data
        BillDTO billDTO = new BillDTO();
        billDTO.setId(1L);
        billEntity = new BillEntity();
        billEntity.setId(1L);

        // BOOKING Data
        bookingDTO = new BookingDTO();
        bookingDTO.setId(1L);
        bookingDTO.setUserDTO(userDTO);
        bookingDTO.setRoomDTO(roomDTO);
        bookingDTO.setBillDTO(billDTO);
        bookingDTO.setCheckIn(LocalDate.now());
        bookingDTO.setCheckOut(LocalDate.now().plusDays(1));
        bookingDTO.setState(BookingStatus.ACTIVE);
        bookingDTO.setCancelled(false);

        bookingEntity = new BookingEntity();
        bookingEntity.setId(1L);
        bookingEntity.setRoomEntity(roomEntity);
        bookingEntity.setUserEntity(userEntity);
        bookingEntity.setBillEntity(billEntity);
        bookingEntity.setCheckIn(LocalDate.now());
        bookingEntity.setCheckOut(LocalDate.now().plusDays(1));
        bookingEntity.setState(BookingStatus.ACTIVE);
        bookingEntity.setCancelled(false);

    }

    @Nested
    @DisplayName("Room Booking Tests")
    class RoomBookingTestCases{
        @Test
        @DisplayName("Should successfully book room when all conditions are met")
        void roomBook_SuccessTest(){
            // Arrange
            when(roomRepository.findById(roomEntity.getRoomNumber())).thenReturn(Optional.of(roomEntity));
            when(userRepository.findById(userEntity.getAdhaarNumber())).thenReturn(Optional.of(userEntity));
            when(billRepository.findById(billEntity.getId())).thenReturn(Optional.of(billEntity));
            when(bookingMapper.dtoToEntity(bookingDTO)).thenReturn(bookingEntity);
            when(bookingRepository.save(bookingEntity)).thenReturn(bookingEntity);
            when(bookingMapper.entityToDTO(bookingEntity)).thenReturn(bookingDTO);

            // Act
            bookingServiceImpl.roomBook(bookingDTO);

            // Assert
            assertThat(roomEntity.getRoomStatus()).isEqualTo(RoomStatus.OCCUPIED);
            // Verify
            verify(bookingRepository,times(1)).save(bookingEntity);
            verify(roomRepository,times(1)).save(roomEntity);
            verify(bookingMapper).dtoToEntity(bookingDTO);
        }

        @Test
        @DisplayName("Should throw exception when room is occupied")
        void roomBook_RoomOccupiedTest(){
            // Given
            roomEntity.setRoomStatus(RoomStatus.OCCUPIED);
            // Arrange
            when(roomRepository.findById(roomEntity.getRoomNumber())).thenReturn(Optional.of(roomEntity));

            // Assert and Throw Exception
            assertThatThrownBy(()-> bookingServiceImpl.roomBook(bookingDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Room not available...");
        }

        @Test
        @DisplayName("Should throw an exception when room is not found")
        void roomBook_RoomNotFoundTest(){
            // Arrange
            when(roomRepository.findById(roomEntity.getRoomNumber())).thenReturn(Optional.empty());
            // Assert and Throw Exception
            assertThatThrownBy(()-> bookingServiceImpl.roomBook(bookingDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Data not found...");
        }

        @Test
        @DisplayName("Should throw exception when check-in date is in past")
        void roomBook_CheckInDateInPastTest(){
            // Given
            bookingDTO.setCheckIn(LocalDate.now().minusDays(1));
            // Assert and Throw Exception
            assertThatThrownBy(()-> bookingServiceImpl.roomBook(bookingDTO))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Check in date can not be previous day...");
        }

        @Test
        @DisplayName("Should throw exception when check-out date is before check-in date")
        void roomBook_CheckOutDateBeforeCheckInDateTest(){
            // Given
            bookingDTO.setCheckIn(LocalDate.now().plusDays(3));
            bookingDTO.setCheckOut(LocalDate.now().plusDays(1));
            // Assert and Throw Exception
            assertThatThrownBy(()-> bookingServiceImpl.roomBook(bookingDTO))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Check Out date must be after check in date...");
        }

        @Test
        @DisplayName("Should throw exception when check-out date equals check-in date")
        void roomBook_CheckOutDateEqualsCheckInDateTest(){
            // Given
            LocalDate sameDate = LocalDate.now().plusDays(1);
            bookingDTO.setCheckIn(sameDate);
            bookingDTO.setCheckOut(sameDate);
            // Assert and Throw Exception
            assertThatThrownBy(()-> bookingServiceImpl.roomBook(bookingDTO))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Check Out date must be after check in date...");
        }

        @Test
        @DisplayName("Should throw exception when check-out date is null")
        void roomBook_CheckOutDateNullTest(){
            // Given
            bookingDTO.setCheckOut(null);
            // Assert and Throw Exception
            assertThatThrownBy(()-> bookingServiceImpl.roomBook(bookingDTO))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Check Out date is required...");
        }
    }

    @Nested
    @DisplayName("Get booking data Tests")
    class GetBookingDataTestCases{
        @Test
        @DisplayName("Should successfully get all booking data")
        void getAllBookings_SuccessTest(){
            // Arrange
            when(bookingRepository.findAll()).thenReturn(List.of(bookingEntity));
            when(bookingMapper.entityToDTO(bookingEntity)).thenReturn(bookingDTO);

            // Act
            List<BookingDTO> result = bookingServiceImpl.getAllBookings();

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(bookingDTO);

            // Verify
            verify(bookingRepository,times(1)).findAll();
            verify(bookingMapper,times(1)).entityToDTO(bookingEntity);
        }

        @Test
        @DisplayName("Should return empty list when no bookings exist")
        void getAllBookings_EmptyListTest(){
            // Arrange
            when(bookingRepository.findAll()).thenReturn(List.of());

            // Act
            List<BookingDTO> result = bookingServiceImpl.getAllBookings();

            // Assert
            assertThat(result).isEmpty();

            // Verify
            verify(bookingRepository,times(1)).findAll();
            verify(bookingMapper,never()).entityToDTO(any());
        }

        @Test
        @DisplayName("Should return booking when exists")
        void getBookingById_SuccessTest(){
            Long id = 1L;
            // Arrange
            when(bookingRepository.findById(id)).thenReturn(Optional.of(bookingEntity));
            when(bookingMapper.entityToDTO(bookingEntity)).thenReturn(bookingDTO);
            // Act
            BookingDTO result = bookingServiceImpl.getBookingById(id);
            // Assert
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(bookingDTO);
            // Verify
            verify(bookingRepository,times(1)).findById(id);
            verify(bookingMapper,times(1)).entityToDTO(bookingEntity);
        }

        @Test
        @DisplayName("Should throw exception when booking not found")
        void getBookingById_NotFoundTest(){
            // Arrange
            when(bookingRepository.findById(1L)).thenReturn(Optional.empty());
            // Assert and Throw Exception
            assertThatThrownBy(()-> bookingServiceImpl.getBookingById(1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(" Data not found...");
        }
    }

    @Nested
    @DisplayName("Update booking Tests")
    class UpdateBookingTestCases{
        @Test
        @DisplayName("Should update booking successfully")
        void updateBooking_SuccessTest(){
            // Given
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(bookingEntity));
            when(loggedInUserService.getCurrentUserEmail()).thenReturn("Email-Test");
            doNothing().when(bookingMapper).updateBooking(bookingEntity,bookingDTO);
            when(roomRepository.findById("TestRoomID1")).thenReturn(Optional.of(roomEntity));
            when(billRepository.findById(1L)).thenReturn(Optional.of(billEntity));
            when(userRepository.findById("Test-Number")).thenReturn(Optional.of(userEntity));
            when(bookingRepository.save(bookingEntity)).thenReturn(bookingEntity);
            when(bookingMapper.entityToDTO(bookingEntity)).thenReturn(bookingDTO);

            // Act
            BookingDTO result = bookingServiceImpl.updateBooking(1L,bookingDTO);

            // Assert
            assertThat(result).isEqualTo(bookingDTO);
            // Verify
            verify(bookingMapper,times(1)).updateBooking(bookingEntity,bookingDTO);
            verify(bookingRepository,times(1)).save(bookingEntity);
        }

        @Test
        @DisplayName("Should throw exception when booking not found for update")
        void updateBooking_NotFoundTest(){
            // Arrange
            when(bookingRepository.findById(1L)).thenReturn(Optional.empty());
            // Assert and throw an exception
            assertThatThrownBy(()-> bookingServiceImpl.updateBooking(1L, bookingDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(" Booking details not found...");
        }

        @Test
        @DisplayName("Should throw exception when room not found during update")
        void updateBooking_RoomNotFoundTest(){
            // Arrange
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(bookingEntity));
            when(loggedInUserService.getCurrentUserEmail()).thenReturn("Email-Test");
            doNothing().when(bookingMapper).updateBooking(bookingEntity,bookingDTO);
            when(roomRepository.findById("TestRoomID1")).thenReturn(Optional.empty());
            // Assert and Throw an exception
            assertThatThrownBy(()-> bookingServiceImpl.updateBooking(1L, bookingDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(" Room details not found...");
        }

        @Test
        @DisplayName("Should throw exception when user not found during update")
        void updateBooking_UserNotFoundTest(){
            // Arrange
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(bookingEntity));
            when(loggedInUserService.getCurrentUserEmail()).thenReturn("Email-Test");
            doNothing().when(bookingMapper).updateBooking(bookingEntity,bookingDTO);
            when(roomRepository.findById("TestRoomID1")).thenReturn(Optional.of(roomEntity));
            when(billRepository.findById(1L)).thenReturn(Optional.of(billEntity));
            when(userRepository.findById("Test-Number")).thenReturn(Optional.empty());
            // Assert and Throw an exception
            assertThatThrownBy(()-> bookingServiceImpl.updateBooking(1L, bookingDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(" User details not found...");
        }

        @Test
        @DisplayName("Should throw exception when bill not found during update")
        void updateBooking_BillNotFound(){
            // Arrange
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(bookingEntity));
            when(loggedInUserService.getCurrentUserEmail()).thenReturn("Email-Test");
            doNothing().when(bookingMapper).updateBooking(bookingEntity,bookingDTO);
            when(roomRepository.findById("TestRoomID1")).thenReturn(Optional.of(roomEntity));
            when(billRepository.findById(1L)).thenReturn(Optional.empty());
            // Assert and Throw an exception
            assertThatThrownBy(()-> bookingServiceImpl.updateBooking(1L, bookingDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(" Bill details not found...");
        }
    }

    @Nested
    @DisplayName("Delete booking Tests")
    class DeleteBookingTestCases{
        @Test
        @DisplayName("Should delete when booking exists")
        void deleteBooking_SuccessTest(){
            // Arrange
            when(bookingRepository.existsById(1L)).thenReturn(true);
            // Act
            bookingServiceImpl.deleteBooking(1L);
            // Verify
            verify(bookingRepository,times(1)).existsById(1L);
            verify(bookingRepository,times(1)).deleteById(1L);
            verifyNoMoreInteractions(bookingRepository);
        }

        @Test
        @DisplayName("Should throw exception when booking not found for deletion")
        void deleteBooking_NotFoundTest(){
            // Arrange
            when(bookingRepository.existsById(1L)).thenReturn(false);
            // Assert and Throw an exception
            assertThatThrownBy(()-> bookingServiceImpl.deleteBooking(1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(" Data not found...");
        }
    }

    @Nested
    @DisplayName("Cancel Booking Tests")
    class CancelBookingTestCases{
        @Test
        @DisplayName("Should cancel booking successfully")
        void cancelBooking_SuccessTest(){
            bookingEntity.setCancelled(false);
            // Arrange
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(bookingEntity));

            // Act
            bookingServiceImpl.cancelBooking(1L);
            // Assert
            assertThat(bookingEntity.isCancelled()).isTrue();
            assertThat(bookingEntity.getState()).isEqualTo(BookingStatus.INACTIVE);
            // Verify
            verify(bookingRepository,times(1)).findById(1L);
            verify(bookingRepository,times(1)).save(bookingEntity);
            verifyNoMoreInteractions(bookingRepository);
        }

        @Test
        @DisplayName("Should throw exception when booking not found for cancellation")
        void cancelBooking_NotFoundTest(){
            // Arrange
            when(bookingRepository.findById(1L)).thenReturn(Optional.empty());
            // Assert and Throw an exception
            assertThatThrownBy(()-> bookingServiceImpl.cancelBooking(1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(" Booking details not found");
        }

        @Test
        @DisplayName("Should throw exception when booking is already cancelled")
        void cancelBooking_AlreadyCancelledTest(){
            // Given
            bookingEntity.setCancelled(true);
            // Arrange
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(bookingEntity));
            // Act
            bookingServiceImpl.cancelBooking(1L);
            // Verify
            verify(bookingRepository,times(1)).findById(1L);
            verify(bookingRepository,never()).save(bookingEntity);
        }
    }

    @Nested
    @DisplayName("Update Check-Out Date Tests")
    class UpdateCheckOutDateTestCases{
        @Test
        @DisplayName("Should update check-out date successfully")
        void updateCheckOutDate_SuccessTest(){
            LocalDate newCheckOutDate = LocalDate.now().plusDays(5);
            // Arrange
            when(loggedInUserService.getCurrentUserEmail()).thenReturn("Email-Test");
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(bookingEntity));
            when(bookingRepository.save(bookingEntity)).thenReturn(bookingEntity);
            when(bookingMapper.entityToDTO(bookingEntity)).thenReturn(bookingDTO);
            // Act
            BookingDTO result = bookingServiceImpl.updateCheckOutDate(1L,newCheckOutDate);
            // Assert
            assertThat(result).isEqualTo(bookingDTO);
            assertThat(bookingEntity.getCheckOut()).isEqualTo(newCheckOutDate);
            assertThat(bookingEntity.getState()).isEqualTo(BookingStatus.ACTIVE);
            // Verify
            verify(bookingRepository,times(1)).findById(1L);
            verify(bookingRepository,times(1)).save(bookingEntity);
            verify(bookingMapper,times(1)).entityToDTO(bookingEntity);
            verifyNoMoreInteractions(bookingRepository,bookingMapper);
        }

        @Test
        @DisplayName("Should throw exception when booking not found for check-out date update")
        void updateCheckOutDate_NotFoundTest(){
            LocalDate newCheckOutDate = LocalDate.now().plusDays(5);
            // Arrange
            when(loggedInUserService.getCurrentUserEmail()).thenReturn("Email-Test");
            when(bookingRepository.findById(1L)).thenReturn(Optional.empty());
            // Assert and Throw an exception
            assertThatThrownBy(()-> bookingServiceImpl.updateCheckOutDate(1L,newCheckOutDate))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage(" Booking details not found...");
            verify(bookingRepository,times(1)).findById(1L);
            verify(bookingRepository,never()).save(bookingEntity);
            verify(bookingMapper,never()).entityToDTO(bookingEntity);
        }

        @Test
        @DisplayName("Should throw exception when user not authorized")
        void updateCheckOutDate_UnAuthorizedTest(){
            LocalDate newCheckOutDate = LocalDate.now().plusDays(5);
            // Arrange
            when(loggedInUserService.getCurrentUserEmail()).thenReturn("Other-Email-Test");
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(bookingEntity));
            // Assert and Throw an exception
            assertThatThrownBy(()-> bookingServiceImpl.updateCheckOutDate(1L,newCheckOutDate))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("You are not allowed to update this booking...");
            verify(bookingRepository,times(1)).findById(1L);
            verify(bookingRepository,never()).save(bookingEntity);
            verify(bookingMapper,never()).entityToDTO(bookingEntity);
        }

        @Test
        @DisplayName("Should throw exception when check-out date is in past")
        void updateCheckOutDate_CheckOutDateInPastTest(){
            LocalDate checkInDate = LocalDate.now().minusDays(10);
            LocalDate newCheckOutDate = LocalDate.now().minusDays(5);
            bookingEntity.setCheckIn(checkInDate);
            // Arrange
            when(loggedInUserService.getCurrentUserEmail()).thenReturn("Email-Test");
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(bookingEntity));
            // Assert and Throw an exception
            assertThatThrownBy(()-> bookingServiceImpl.updateCheckOutDate(1L,newCheckOutDate))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Check Out date can not be in past...");
        }

        @Test
        @DisplayName("Should throw exception when check-out date is before check-in")
        void updateCheckOutDate_CheckOutDateBeforeCheckInTest(){
            LocalDate checkInDate = LocalDate.now().plusDays(10);
            LocalDate newCheckOutDate = LocalDate.now().minusDays(5);
            bookingEntity.setCheckIn(checkInDate);
            // Arrange
            when(loggedInUserService.getCurrentUserEmail()).thenReturn("Email-Test");
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(bookingEntity));
            // Assert and Throw an exception
            assertThatThrownBy(()-> bookingServiceImpl.updateCheckOutDate(1L,newCheckOutDate))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Check Out date must be after check in date...");
        }

        @Test
        @DisplayName("Should throw exception when check-out date equals check-in")
        void updateCheckOutDate_CheckOutDateEqualsCheckInTest(){
            LocalDate checkInDate = LocalDate.now().plusDays(5);
            LocalDate newCheckOutDate = LocalDate.now().plusDays(5);
            bookingEntity.setCheckIn(checkInDate);
            // Arrange
            when(loggedInUserService.getCurrentUserEmail()).thenReturn("Email-Test");
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(bookingEntity));
            // Assert and Throw an exception
            assertThatThrownBy(()-> bookingServiceImpl.updateCheckOutDate(1L,newCheckOutDate))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Check Out date must be after check in date...");
        }
    }

    @Nested
    @DisplayName("Set Booking Term Tests")
    class SetBookingTermTests{
        @Test
        @DisplayName("Should set booking as ACTIVE when check-out is in future")
        void setBookingTerm_ActiveTest(){
            bookingDTO.setCheckOut(LocalDate.now().plusDays(1));
            bookingDTO.setState(BookingStatus.INACTIVE);
            // Act
            bookingServiceImpl.setBookingTerm(bookingDTO);
            // Assert
            assertThat(bookingDTO.getState()).isEqualTo(BookingStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should set booking as ACTIVE when check-out is today")
        void setBookingTerm_TodayTest(){
            bookingDTO.setCheckOut(LocalDate.now());
            bookingDTO.setState(BookingStatus.INACTIVE);
            // Act
            bookingServiceImpl.setBookingTerm(bookingDTO);
            // Assert
            assertThat(bookingDTO.getState()).isEqualTo(BookingStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should set booking as INACTIVE when check-out is in past")
        void setBookingTerm_InActiveTest(){
            bookingDTO.setCheckOut(LocalDate.now().minusDays(1));
            bookingDTO.setState(BookingStatus.ACTIVE);
            // Act
            bookingServiceImpl.setBookingTerm(bookingDTO);
            // Assert
            assertThat(bookingDTO.getState()).isEqualTo(BookingStatus.INACTIVE);
        }
    }
}
