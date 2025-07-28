package com.project.aptflow.dto.apiresponse;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Setter
@Getter
public class ResponseDTO <T>{
    @JsonProperty("message")
    private String message;
    @JsonProperty("status")
    private String status;
    @JsonProperty("timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    @JsonProperty("data")
    private T data;

    public ResponseDTO(String message, HttpStatus status, T data) {
        this.message = message;
        this.status = status.name();
        this.timestamp = LocalDateTime.now();
        this.data = data;
    }
}
