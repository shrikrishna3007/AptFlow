package com.project.aptflow.entity;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Setter
@Getter
@Document(collection = "room_images")
@AllArgsConstructor
@NoArgsConstructor
public class RoomImagesEntity {
    @Id
    private String id;
    @Field("room_number")
    private String roomNumber;
    @Field("images")
    private List<String> imageIDs;
}
