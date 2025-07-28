package com.project.aptflow.globalexceptionhandler;

import com.project.aptflow.dto.apiresponse.MessageResponseDTO;
import com.project.aptflow.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<MessageResponseDTO> handleResourceNotFound(ResourceNotFoundException exception){
        MessageResponseDTO response= new MessageResponseDTO(exception.getMessage(), HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<MessageResponseDTO> handleBadRequest(BadRequestException exception){
        MessageResponseDTO response= new MessageResponseDTO(exception.getMessage(), HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PersistenceException.class)
    public ResponseEntity<MessageResponseDTO> handlePersistenceError(PersistenceException exception){
        MessageResponseDTO response= new MessageResponseDTO(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UnAuthorizedException.class)
    public ResponseEntity<MessageResponseDTO> handleUnauthorizedAccess(UnAuthorizedException exception){
        MessageResponseDTO response= new MessageResponseDTO(exception.getMessage(), HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<MessageResponseDTO> handleAccessDenied(AccessDeniedException exception){
        MessageResponseDTO response= new MessageResponseDTO(exception.getMessage(), HttpStatus.FORBIDDEN);
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<MessageResponseDTO> handleMessagingException(MessagingException exception){
        MessageResponseDTO response= new MessageResponseDTO(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(OrderCreationException.class)
    public ResponseEntity<MessageResponseDTO> handleOrderCreationException(OrderCreationException exception){
        MessageResponseDTO response= new MessageResponseDTO(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(PaymentVerificationException.class)
    public ResponseEntity<MessageResponseDTO> handlePaymentVerificationException(PaymentVerificationException exception){
        MessageResponseDTO response= new MessageResponseDTO(exception.getMessage(), HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HmacGenerationException.class)
    public ResponseEntity<MessageResponseDTO> handleHmacGenerationException(HmacGenerationException exception){
        MessageResponseDTO response= new MessageResponseDTO(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
