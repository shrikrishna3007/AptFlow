package com.project.aptflow.unittest.service;

import com.project.aptflow.dto.BillDTO;
import com.project.aptflow.dto.RoomDTO;
import com.project.aptflow.dto.RoomManagementDTO;
import com.project.aptflow.dto.RoomWithImagesDTO;
import com.project.aptflow.entity.BillEntity;
import com.project.aptflow.entity.RoomEntity;
import com.project.aptflow.entity.RoomImagesEntity;
import com.project.aptflow.exceptions.BadRequestException;
import com.project.aptflow.exceptions.PersistenceException;
import com.project.aptflow.exceptions.ResourceNotFoundException;
import com.project.aptflow.mapper.BillMapper;
import com.project.aptflow.mapper.RoomWithImagesMapper;
import com.project.aptflow.repository.BillRepository;
import com.project.aptflow.repository.RoomImageRepository;
import com.project.aptflow.repository.RoomRepository;
import com.project.aptflow.service.impl.RoomManagementServiceImpl;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("Room Management Service Unit Tests")
public class RoomManagementServiceUnitTest {

    @Mock
    private RoomRepository roomRepository;
    @Mock
    private RoomWithImagesMapper roomWithImagesMapper;
    @Mock
    private RoomImageRepository roomImageRepository;
    @Mock
    private BillRepository billRepository;
    @Mock
    private BillMapper billMapper;
    @Mock
    private GridFsTemplate gridFsTemplate;

    @InjectMocks
    private RoomManagementServiceImpl roomManagementServiceImpl;

    private RoomManagementDTO roomManagementDTO;
    private MultipartFile[] images;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);

        RoomDTO roomDTO = new RoomDTO();
        roomDTO.setRoomNumber("RoomID-Test");

        BillDTO billDTO = new BillDTO();
        billDTO.setId(1L);

        RoomWithImagesDTO roomWithImagesDTO = new RoomWithImagesDTO();
        roomWithImagesDTO.setRoomDTO(roomDTO);

        roomManagementDTO = new RoomManagementDTO();
        roomManagementDTO.setRoomWithImagesDTO(roomWithImagesDTO);
        roomManagementDTO.setBillDTO(billDTO);

        images = new MultipartFile[]{
                new MockMultipartFile("images", "room1.jpg", "image/jpeg", "dummy1".getBytes())
        };
    }

    @Test
    void addRoomWithImages_SuccessTest(){
        // Mock mapped entities
        RoomEntity room = new RoomEntity();
        BillEntity bill = new BillEntity();
        RoomImagesEntity imagesEntity = new RoomImagesEntity();

        // Mock mapper behaviour
        when(roomWithImagesMapper.dtoToEntity(any())).thenReturn(room);
        when(billMapper.dtoToEntity(any())).thenReturn(bill);
        when(roomWithImagesMapper.dtoToImageEntity(any(), any())).thenReturn(imagesEntity);

        // Mock repositories and GridFsTemplate behaviour
        when(gridFsTemplate.store(any(InputStream.class),anyString()))
                .thenReturn(new ObjectId("507f1f77bcf86cd799439011")); // Mock ObjectId

        // Act
        roomManagementServiceImpl.addRoomWithImages(roomManagementDTO, images);

        // Verify
        verify(roomRepository, times(1)).save(room);
        verify(billRepository, times(1)).save(bill);
        verify(gridFsTemplate,times(1)).store(any(InputStream.class),anyString());
        verify(roomImageRepository, times(1)).save(imagesEntity);
    }

    @Test
    void addRoomWithImages_ValidationFailureTest_1(){
        roomManagementDTO.setRoomWithImagesDTO(null);

        assertThatThrownBy(() -> roomManagementServiceImpl.addRoomWithImages(roomManagementDTO, images))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Room details must be provided");
    }

    @Test
    void addRoomWithImages_ValidationFailureTest_2(){
        roomManagementDTO.setBillDTO(null);

        assertThatThrownBy(() -> roomManagementServiceImpl.addRoomWithImages(roomManagementDTO, images))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Bill details must be provided");
    }

    @Test
    void addRoomWithImages_ValidationFailureTest_3(){
        images = null;

        assertThatThrownBy(()->roomManagementServiceImpl.addRoomWithImages(roomManagementDTO, images))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Images must be provided");
    }

    @Test
    void addRoomWithImages_GridFsTemplateFailureTest() throws IOException {
        // Set up entities
        RoomEntity room = new RoomEntity();
        BillEntity bill = new BillEntity();

        when(roomWithImagesMapper.dtoToEntity(any())).thenReturn(room);
        when(billMapper.dtoToEntity(any())).thenReturn(bill);

        MockMultipartFile image = mock(MockMultipartFile.class);
        when(image.getInputStream()).thenThrow(new IOException("Error while reading image."));
        MultipartFile[] images = new MultipartFile[]{image};


        assertThatThrownBy(() -> roomManagementServiceImpl.addRoomWithImages(roomManagementDTO, images))
                .isInstanceOf(PersistenceException.class)
                .hasMessageContaining("Error storing images.");
    }

    @Test
    void deleteRoomWithImages_SuccessTest(){
        String roomNumber = "RoomID-Test";
        RoomEntity room = new RoomEntity();
        RoomImagesEntity imagesEntity = new RoomImagesEntity();
        imagesEntity.setImageIDs(List.of("image1", "image2", "image3"));

        when(roomRepository.findById(roomNumber)).thenReturn(Optional.of(room));
        when(roomImageRepository.findByRoomNumber(roomNumber)).thenReturn(Optional.of(imagesEntity));

        // Act
        roomManagementServiceImpl.deleteRoomWithImages(roomNumber);

        // Verify
        verify(gridFsTemplate,times(1)).delete(new Query(Criteria.where("_id").is("image1")));
        verify(gridFsTemplate,times(1)).delete(new Query(Criteria.where("_id").is("image2")));
        verify(gridFsTemplate,times(1)).delete(new Query(Criteria.where("_id").is("image3")));

        verify(roomImageRepository,times(1)).delete(imagesEntity);
        verify(roomRepository,times(1)).delete(room);
    }

    @Test
    void deleteRoomWithImages_RoomNotFoundTest(){
        String roomNumber = "RoomID-Test";

        when(roomRepository.findById(roomNumber)).thenReturn(Optional.empty());

        assertThatThrownBy(()->roomManagementServiceImpl.deleteRoomWithImages(roomNumber))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Room not found...");
        // Verify
        verify(roomImageRepository,never()).findByRoomNumber(any());
        verify(gridFsTemplate,never()).delete(any());
        verify(roomImageRepository,never()).delete(any());
        verify(roomRepository,never()).delete(any());
    }

    @Test
    void deleteRoomWithImages_ImagesNotFoundTest(){
        String roomNumber = "RoomID-Test";
        RoomEntity room = new RoomEntity();

        when(roomRepository.findById(roomNumber)).thenReturn(Optional.of(room));
        when(roomImageRepository.findByRoomNumber(roomNumber)).thenReturn(Optional.empty());

        assertThatThrownBy(()-> roomManagementServiceImpl.deleteRoomWithImages(roomNumber))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Images not found...");

        // Verify
        verify(gridFsTemplate,never()).delete(any());
        verify(roomImageRepository,never()).delete(any());
        verify(roomRepository,never()).delete(any());

    }
}
