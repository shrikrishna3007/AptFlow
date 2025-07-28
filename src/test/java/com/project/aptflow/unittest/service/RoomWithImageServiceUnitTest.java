package com.project.aptflow.unittest.service;

import com.project.aptflow.dto.RoomDTO;
import com.project.aptflow.dto.RoomWithImagesDTO;
import com.project.aptflow.entity.RoomEntity;
import com.project.aptflow.entity.RoomImagesEntity;
import com.project.aptflow.enums.RoomStatus;
import com.project.aptflow.exceptions.PersistenceException;
import com.project.aptflow.exceptions.ResourceNotFoundException;
import com.project.aptflow.mapper.RoomWithImagesMapper;
import com.project.aptflow.repository.RoomImageRepository;
import com.project.aptflow.repository.RoomRepository;
import com.project.aptflow.service.impl.RoomWithImagesServiceImpl;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@DisplayName("RoomWithImageService Unit Tests")
class RoomWithImageServiceUnitTest {
    private static final String RESOURCE_NOT_FOUND_MESSAGE = "Data not found...";
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private RoomImageRepository roomImageRepository;
    @Mock
    private RoomWithImagesMapper roomWithImagesMapper;
    @Mock
    private GridFsTemplate gridFsTemplate;

    @InjectMocks
    private RoomWithImagesServiceImpl roomWithImagesService;

    private RoomEntity roomEntity;
    private RoomImagesEntity roomImagesEntity;
    private RoomWithImagesDTO roomWithImagesDTO;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);

        roomEntity = new RoomEntity();
        roomEntity.setRoomNumber("TestRoomID1");
        roomEntity.setRoomType("Room-Type");
        roomEntity.setRoomCapacity(2);
        roomEntity.setRoomDescription("Room-Description");
        roomEntity.setRoomStatus(RoomStatus.AVAILABLE);
        roomEntity.setRoomRent(new BigDecimal("1000"));
        roomEntity.setRoomSharing(true);

        // Create RoomImagesEntity
        roomImagesEntity = new RoomImagesEntity();
        roomImagesEntity.setRoomNumber("TestRoomID1");
        roomImagesEntity.setImageIDs(List.of("image1", "image2"));

        // Create RoomDTO
        RoomDTO roomDTO = new RoomDTO();
        roomDTO.setRoomNumber("TestRoomID1");
        roomDTO.setRoomType("Room-Type");
        roomDTO.setRoomCapacity(2);
        roomDTO.setRoomDescription("Room-Description");
        roomDTO.setRoomStatus(RoomStatus.AVAILABLE);
        roomDTO.setRoomRent(new BigDecimal(1000));
        roomDTO.setRoomSharing(true);

        // Create RoomWithImagesDTO
        roomWithImagesDTO = new RoomWithImagesDTO();
        roomWithImagesDTO.setRoomDTO(roomDTO);
        roomWithImagesDTO.setImageIDs(List.of("image1", "image2"));
    }

    @Test
    void getRoomDetailsByRoomNumber_SuccessTest() {
        String roomNumber = "TestRoomID1";

        RoomEntity room = new RoomEntity();
        RoomImagesEntity imagesEntity = new RoomImagesEntity();
        RoomWithImagesDTO roomWithImagesDTO = new RoomWithImagesDTO();

        // Mock repository and mapper behaviour
        when(roomRepository.findById(roomNumber)).thenReturn(Optional.of(room));
        when(roomImageRepository.findByRoomNumber(roomNumber)).thenReturn(Optional.of(imagesEntity));
        when(roomWithImagesMapper.entityToDTO(room,imagesEntity)).thenReturn(roomWithImagesDTO);
        // Act
        RoomWithImagesDTO result = roomWithImagesService.getRoomDetailsByRoomNumber(roomNumber);
        // Assert
        assertThat(result).isEqualTo(roomWithImagesDTO);
        assertNotNull(result);
        // Verify
        verify(roomRepository,times(1)).findById(roomNumber);
        verify(roomImageRepository,times(1)).findByRoomNumber(roomNumber);
        verify(roomWithImagesMapper,times(1)).entityToDTO(room,imagesEntity);
        verifyNoMoreInteractions(roomRepository,roomImageRepository,roomWithImagesMapper);
    }

    @Test
    void getRoomDetailsByRoomNumber_RoomNotFoundTest(){
        String roomNumber = "TestRoomID1";
        // Mock room repository behaviour
        when(roomRepository.findById(roomNumber)).thenReturn(Optional.empty());
        // Act
        assertThatThrownBy(()->roomWithImagesService.getRoomDetailsByRoomNumber(roomNumber))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(RESOURCE_NOT_FOUND_MESSAGE);
        // Verify
        verify(roomRepository,times(1)).findById(roomNumber);
        verify(roomImageRepository,never()).findByRoomNumber(roomNumber);
        verify(roomWithImagesMapper,never()).entityToDTO(any(),any());
        verifyNoMoreInteractions(roomRepository,roomImageRepository,roomWithImagesMapper);
    }

    @Test
    void getRoomDetailsByRoomNumber_ImagesNotFoundTest() {
        String roomNumber = "TestRoomID1";
        // Mock room repository behaviour
        when(roomRepository.findById(roomNumber)).thenReturn(Optional.of(new RoomEntity()));
        // Act
        assertThatThrownBy(() -> roomWithImagesService.getRoomDetailsByRoomNumber(roomNumber))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(RESOURCE_NOT_FOUND_MESSAGE);
        // Verify
        verify(roomRepository, times(1)).findById(roomNumber);
        verify(roomImageRepository, times(1)).findByRoomNumber(roomNumber);
        verify(roomWithImagesMapper, never()).entityToDTO(any(), any());
        verifyNoMoreInteractions(roomRepository, roomImageRepository, roomWithImagesMapper);
    }

    @Test
    void getAllRoomDetails_SuccessTest() {
        // Mock repositories and mapper
        when(roomRepository.findAll()).thenReturn(List.of(roomEntity));
        when(roomImageRepository.findByRoomNumber(roomEntity.getRoomNumber())).thenReturn(Optional.of(roomImagesEntity));
        when(roomWithImagesMapper.entityToDTO(roomEntity, roomImagesEntity)).thenReturn(roomWithImagesDTO);
        // Act
        List<RoomWithImagesDTO> result = roomWithImagesService.getAllRoomDetails();
        // Assert
        assertThat(result).hasSize(1)
                .isEqualTo(List.of(roomWithImagesDTO));
        assertThat(result.get(0).getRoomDTO().getRoomNumber()).isEqualTo("TestRoomID1");
        assertThat(List.of("image1", "image2")).isEqualTo(result.get(0).getImageIDs());
        // Verify
        verify(roomRepository, times(1)).findAll();
        verify(roomImageRepository, times(1)).findByRoomNumber(roomEntity.getRoomNumber());
        verify(roomWithImagesMapper, times(1)).entityToDTO(roomEntity, roomImagesEntity);
        verifyNoMoreInteractions(roomRepository, roomImageRepository, roomWithImagesMapper);
    }

    @Test
    void getAllRoomDetails_EmptyListTest() {
        // Mock repositories and mapper
        when(roomRepository.findAll()).thenReturn(List.of());
        // Act
        List<RoomWithImagesDTO> result = roomWithImagesService.getAllRoomDetails();
        // Assert
        assertThat(result).isEmpty();
        // Verify
        verify(roomRepository, times(1)).findAll();
        verify(roomImageRepository, never()).findByRoomNumber(anyString());
        verify(roomWithImagesMapper, never()).entityToDTO(any(), any());
        verifyNoMoreInteractions(roomRepository, roomImageRepository, roomWithImagesMapper);
    }

    @Test
    void getAllRoomDetails_RoomImagesNotFoundTest() {
        // Mock room repository behaviour
        when(roomRepository.findAll()).thenReturn(List.of(roomEntity));
        // Mock room image repository behaviour
        when(roomImageRepository.findByRoomNumber(roomEntity.getRoomNumber())).thenReturn(Optional.empty());
        // Assert
        assertThatThrownBy(()->roomWithImagesService.getAllRoomDetails())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(RESOURCE_NOT_FOUND_MESSAGE);
        // Verify
        verify(roomRepository,times(1)).findAll();
        verify(roomImageRepository,times(1)).findByRoomNumber(roomEntity.getRoomNumber());
        verify(roomWithImagesMapper,never()).entityToDTO(any(),any());
        verifyNoMoreInteractions(roomRepository,roomImageRepository,roomWithImagesMapper);
    }

    @Test
    void getAvailableRooms_SuccessTest(){
        // Arrange
        RoomStatus status = RoomStatus.AVAILABLE;
        // Mock repositories methods.
        when(roomRepository.findByRoomStatus(status)).thenReturn(List.of(roomEntity));
        when(roomImageRepository.findByRoomNumber(roomEntity.getRoomNumber())).thenReturn(Optional.of(roomImagesEntity));
        when(roomWithImagesMapper.entityToDTO(roomEntity,roomImagesEntity)).thenReturn(roomWithImagesDTO);
        // Act
        List<RoomWithImagesDTO> result = roomWithImagesService.getAvailableRooms(status);
        // Assert
        assertThat(result).isNotNull().hasSize(1).containsExactly(roomWithImagesDTO);
        // Verify
        verify(roomRepository,times(1)).findByRoomStatus(status);
        verify(roomImageRepository,times(1)).findByRoomNumber(roomEntity.getRoomNumber());
        verify(roomWithImagesMapper,times(1)).entityToDTO(roomEntity,roomImagesEntity);
        verifyNoMoreInteractions(roomRepository,roomImageRepository,roomWithImagesMapper);
    }

    @Test
    void getAvailableRooms_RoomsNotFoundTest(){
        // Arrange
        RoomStatus status = RoomStatus.AVAILABLE;
        // Mock repository method
        when(roomRepository.findByRoomStatus(status)).thenReturn(List.of());
        // Act and Assert
        assertThatThrownBy(()->roomWithImagesService.getAvailableRooms(status))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No available rooms found...");
        // Verify
        verify(roomRepository,times(1)).findByRoomStatus(status);
        verifyNoMoreInteractions(roomRepository);
    }

    @Test
    void getAvailableRooms_ImagesNotFoundTest(){
        // Arrange
        RoomStatus status = RoomStatus.AVAILABLE;
        // Mock repositories method
        when(roomRepository.findByRoomStatus(status)).thenReturn(List.of(roomEntity));
        when(roomImageRepository.findByRoomNumber(roomEntity.getRoomNumber())).thenReturn(Optional.empty());
        // Act and Assert
        assertThatThrownBy(()->roomWithImagesService.getAvailableRooms(status))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No images found....");
        // Verify
        verify(roomRepository,times(1)).findByRoomStatus(status);
        verify(roomImageRepository,times(1)).findByRoomNumber(roomEntity.getRoomNumber());
        verifyNoMoreInteractions(roomRepository,roomImageRepository);
    }

    @Test
    void updateRoomDetailsWithImages_SuccessTest() throws IOException {
        // Given
        String roomNumber ="TestID1";
        MultipartFile file1= mock(MultipartFile.class);
        MultipartFile file2 = mock(MultipartFile.class);
        MultipartFile[] files = new MultipartFile[]{file1,file2};
        // Mock file content
        when(file1.getInputStream()).thenReturn(new ByteArrayInputStream("image1".getBytes()));
        when(file2.getInputStream()).thenReturn(new ByteArrayInputStream("image2".getBytes()));
        // Mock repository methods
        when(roomRepository.findById(roomNumber)).thenReturn(Optional.of(roomEntity));
        when(roomImageRepository.findByRoomNumber(roomNumber)).thenReturn(Optional.of(roomImagesEntity));

        // Mock existing image IDs
        roomImagesEntity.setImageIDs(List.of("existingImage1", "existingImage2"));
        // Simulate storing new images
        when(gridFsTemplate.store(any(InputStream.class),anyString())).thenReturn(new ObjectId("507f1f77bcf86cd799439011"));

        // Mapper methods
        doNothing().when(roomWithImagesMapper).updateRoomEntity(eq(roomEntity),eq(roomWithImagesDTO));
        when(roomWithImagesMapper.entityToDTO(roomEntity,roomImagesEntity)).thenReturn(roomWithImagesDTO);

        // Act
        RoomWithImagesDTO result = roomWithImagesService.updateRoomDetailsWithImages(roomNumber,roomWithImagesDTO,files);

        // Assert
        assertThat(result).isNotNull()
                .isEqualTo(roomWithImagesDTO);

        // Verify
        verify(roomRepository,times(1)).findById(roomNumber);
        verify(roomImageRepository,times(1)).findByRoomNumber(roomNumber);
        verify(gridFsTemplate, times(2)).delete(any(Query.class));
        verify(gridFsTemplate,times(2)).store(any(InputStream.class),anyString());
        verify(roomImageRepository,times(1)).save(any(RoomImagesEntity.class));
        verify(roomWithImagesMapper).entityToDTO(roomEntity,roomImagesEntity);
    }

    @Test
    void updateRoomDetailsWithImages_RoomNotFoundTest(){
        String roomNumber = "TestID1";
        // Arrange
        when(roomRepository.findById(roomNumber)).thenReturn(Optional.empty());
        // Act and Assert
        assertThatThrownBy(()->roomWithImagesService.updateRoomDetailsWithImages(roomNumber,roomWithImagesDTO,new MultipartFile[0]))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(RESOURCE_NOT_FOUND_MESSAGE);
        // Verify
        verify(roomRepository,times(1)).findById(roomNumber);
        verifyNoMoreInteractions(roomRepository);
    }

    @Test
    void updateRoomDetailsWithImages_ImagesNotFoundTest(){
        String roomNumber = "TestID1";
        // Arrange
        when(roomRepository.findById(roomNumber)).thenReturn(Optional.of(roomEntity));
        when(roomImageRepository.findByRoomNumber(roomNumber)).thenReturn(Optional.empty());

        // Act and Assert
        assertThatThrownBy(()->roomWithImagesService.updateRoomDetailsWithImages(roomNumber,roomWithImagesDTO,new MultipartFile[0]))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Room images with room number not found");
        // Verify
        verify(roomRepository,times(1)).findById(roomNumber);
        verify(roomImageRepository,times(1)).findByRoomNumber(roomNumber);

        verify(roomWithImagesMapper,never()).updateRoomEntity(any(),any());
        verify(roomRepository,never()).save(any());
        verify(gridFsTemplate,never()).store(any(),anyString());
        verify(roomImageRepository,never()).save(any());
        verifyNoMoreInteractions(roomRepository,roomImageRepository,roomWithImagesMapper,gridFsTemplate);
    }

    @Test
    void updateRoomDetailsWithImages_IOExceptionTest() throws IOException {
        String roomNumber = "TestID1";
        MultipartFile file = mock(MultipartFile.class);
        MultipartFile[] files = new MultipartFile[]{file};
        roomImagesEntity.setImageIDs(List.of("existingImage1", "existingImage2"));
        // Mock repository methods
        when(roomRepository.findById(roomNumber)).thenReturn(Optional.of(roomEntity));
        when(roomImageRepository.findByRoomNumber(roomNumber)).thenReturn(Optional.of(roomImagesEntity));
        // Simulate IOException
        when(file.getInputStream()).thenThrow(new IOException("Test IOException"));

        // Act and Assert
        assertThatThrownBy(()->roomWithImagesService.updateRoomDetailsWithImages(roomNumber,roomWithImagesDTO,files))
                .isInstanceOf(PersistenceException.class)
                .hasMessageContaining("Error storing images...");
        // Verify
        verify(roomRepository,times(1)).findById(roomNumber);
        verify(roomImageRepository,times(1)).findByRoomNumber(roomNumber);
        verify(file).getInputStream();
        verify(gridFsTemplate).delete(new Query(Criteria.where("_id").is("existingImage1")));
        verify(gridFsTemplate).delete(new Query(Criteria.where("_id").is("existingImage2")));
        verify(gridFsTemplate,never()).store(any(),anyString());
        verify(roomImageRepository,never()).save(any());
        verify(roomWithImagesMapper,never()).entityToDTO(any(),any());
    }

    /*
    In service logic there is a code to delete existing images from gridFS Before saving new images.
    That scenario will be covered in this test case.
     */
    @Test
    void updateRoomDetailsWithImages_NoExistingImagesDeleteNothingTest() throws IOException {
        String roomNumber = "TestID1";
        MultipartFile file1 = mock(MultipartFile.class);
        MultipartFile[] files = new MultipartFile[]{file1};

        // Read file content
        when(file1.getInputStream()).thenReturn(new ByteArrayInputStream("image1".getBytes()));

        // Mock repository methods
        when(roomRepository.findById(roomNumber)).thenReturn(Optional.of(roomEntity));
        // Set existing image IDs to null
        roomImagesEntity.setImageIDs(null);
        when(roomImageRepository.findByRoomNumber(roomNumber)).thenReturn(Optional.of(roomImagesEntity));

        when(gridFsTemplate.store(any(InputStream.class),anyString())).thenReturn(new ObjectId("507f1f77bcf86cd799439011"));
        doNothing().when(roomWithImagesMapper).updateRoomEntity(eq(roomEntity),eq(roomWithImagesDTO));
        when(roomWithImagesMapper.entityToDTO(roomEntity,roomImagesEntity)).thenReturn(roomWithImagesDTO);

        // Act
        RoomWithImagesDTO result = roomWithImagesService.updateRoomDetailsWithImages(roomNumber,roomWithImagesDTO,files);
        // Assert
        assertThat(result).isNotNull()
                .isEqualTo(roomWithImagesDTO);

        // Verify: No delete operation since existing image is set to null
        verify(gridFsTemplate,never()).delete(any(Query.class));
        verify(gridFsTemplate).store(any(InputStream.class),anyString());
        verify(roomImageRepository).save(roomImagesEntity);
    }
}
