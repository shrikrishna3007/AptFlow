package com.project.aptflow.unittest.service;

import com.project.aptflow.dto.BillDTO;
import com.project.aptflow.entity.BillEntity;
import com.project.aptflow.entity.RoomEntity;
import com.project.aptflow.exceptions.ResourceNotFoundException;
import com.project.aptflow.mapper.BillMapper;
import com.project.aptflow.repository.BillRepository;
import com.project.aptflow.service.impl.BillServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DisplayName("Bill Service Unit Tests")
public class BillServiceUnitTest {
    @Mock
    private BillRepository billRepository;

    @Mock
    private BillMapper billMapper;

    @InjectMocks
    private BillServiceImpl billServiceImpl;

    private BillEntity billEntity;
    private BillDTO billDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        RoomEntity roomEntity = new RoomEntity();
        roomEntity.setRoomNumber("TestRoomID1");

        billEntity = new BillEntity();
        billEntity.setId(1L);
        billEntity.setRoom(roomEntity);
        billEntity.setMonth("2025-06");
        billEntity.setElectricityBill(new BigDecimal(1000));
        billEntity.setUnitPrice(new BigDecimal(2));
        billEntity.setRentPerDay(new BigDecimal(500));

        billDTO = new BillDTO();
        billDTO.setId(1L);
        billDTO.setRoomNumber(roomEntity.getRoomNumber());
        billDTO.setMonth("2025-06");
        billDTO.setElectricityBill(new BigDecimal(1000));
        billDTO.setUnitPrice(new BigDecimal(2));
        billDTO.setRentPerDay(new BigDecimal(500));
    }

    @Nested
    @DisplayName("Get Bills Tests")
    class GetBillsTests {
        @Test
        @DisplayName("Get All Bills")
        void getAllBills_SuccessTest(){
            List<BillEntity> billEntities = List.of(billEntity);
            List<BillDTO> billDTOs = List.of(billDTO);
            // Arrange
            when(billRepository.findAll()).thenReturn(billEntities);
            when(billMapper.entityToDTO(billEntity)).thenReturn(billDTO);
            // Act
            List<BillDTO> result = billServiceImpl.getAllBills();
            // Assert
            assertThat(result).hasSize(1);
            assertThat(result).isEqualTo(billDTOs);
            assertThat(result.get(0).getRoomNumber()).isEqualTo("TestRoomID1");
            // Verify
            verify(billRepository,times(1)).findAll();
            verify(billMapper,times(1)).entityToDTO(billEntity);
            verifyNoMoreInteractions(billRepository,billMapper);
        }

        @Test
        @DisplayName("Get All Bills Empty List")
        void getAllBills_EmptyListTest(){
            List<BillEntity> billEntities = List.of();
            List<BillDTO> billDTOs = List.of();
            // Arrange
            when(billRepository.findAll()).thenReturn(billEntities);
            // Act
            List<BillDTO> result = billServiceImpl.getAllBills();
            // Assert
            assertThat(result).hasSize(0);
            assertThat(result).isEqualTo(billDTOs);
            // Verify
            verify(billRepository,times(1)).findAll();
            verifyNoMoreInteractions(billRepository);
        }

        @Test
        @DisplayName("Get Bill By Id")
        void getBillById_SuccessTest(){
            // Arrange
            when(billRepository.findById(1L)).thenReturn(Optional.of(billEntity));
            when(billMapper.entityToDTO(billEntity)).thenReturn(billDTO);
            // Act
            BillDTO result = billServiceImpl.getBillById(1L);
            // Assert
            assertThat(result).isEqualTo(billDTO);
            // Verify
            verify(billRepository,times(1)).findById(1L);
            verify(billMapper,times(1)).entityToDTO(billEntity);
            verifyNoMoreInteractions(billRepository,billMapper);
        }

        @Test
        @DisplayName("Get Bill By Id Failure: Bill Not Found")
        void getBillById_FailureTest(){
            // Arrange
            when(billRepository.findById(1L)).thenReturn(Optional.empty());
            // Act & Assert
            assertThatThrownBy(() -> billServiceImpl.getBillById(1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Bill not found...");
            // Verify
            verify(billRepository,times(1)).findById(1L);
            verifyNoMoreInteractions(billRepository);
        }
    }

    @Nested
    @DisplayName("Update Bill Tests")
    class UpdateBillTests {
        @Test
        @DisplayName("Update Bill Success")
        void updateBill_SuccessTest(){
            // Arrange
            when(billRepository.findById(1L)).thenReturn(Optional.of(billEntity));
            when(billRepository.save(billEntity)).thenReturn(billEntity);
            when(billMapper.entityToDTO(billEntity)).thenReturn(billDTO);
            // Act
            BillDTO result = billServiceImpl.updateBill(1L,billDTO);
            // Assert
            assertThat(result).isEqualTo(billDTO);
            // Verify
            verify(billRepository,times(1)).findById(1L);
            verify(billMapper,times(1)).updateBillEntity(billEntity,billDTO);
            verify(billRepository,times(1)).save(billEntity);
            verify(billMapper,times(1)).entityToDTO(billEntity);
            verifyNoMoreInteractions(billRepository,billMapper);
        }

        @Test
        @DisplayName("Update Bill Failure: Bill Not Found")
        void updateBill_FailureTest(){
            // Arrange
            when(billRepository.findById(1L)).thenReturn(Optional.empty());
            // Act & Assert
            assertThatThrownBy(() -> billServiceImpl.updateBill(1L,billDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Bill not found...");
            // Verify
            verify(billRepository,times(1)).findById(1L);
            verifyNoMoreInteractions(billRepository);
        }
    }
}
