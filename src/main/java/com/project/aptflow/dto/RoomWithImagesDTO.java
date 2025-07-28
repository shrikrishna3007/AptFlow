package com.project.aptflow.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class RoomWithImagesDTO {
    private RoomDTO roomDTO;
    private List<String> imageIDs;
}
