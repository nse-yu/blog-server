package com.example.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@ControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(value = {ApiRequestException.class})
    public ResponseEntity<Object> handleApiRequestException(ApiRequestException e){
        //1. create payload containing exception details
        HttpStatus badRequest = HttpStatus.BAD_REQUEST;

        ApiExcetion apiExcetion = new ApiExcetion(
                e.getClass().toString(),
                e,
                badRequest,
                ZonedDateTime.now(ZoneId.of("Z"))
        );
        //2. return response entity
        //constructor for body and status
        return new ResponseEntity<>(apiExcetion, badRequest);
    }
}
