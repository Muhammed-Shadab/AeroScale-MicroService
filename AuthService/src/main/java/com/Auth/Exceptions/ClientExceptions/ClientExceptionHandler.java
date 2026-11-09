package com.Auth.Exceptions.ClientExceptions;


import com.Auth.Exceptions.AuthException.UserAlreadyExistsException;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

@RestControllerAdvice
public class ClientExceptionHandler {

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<?> userAlreadyExistsExceptionHandler(HttpClientErrorException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Error Happend With RestClient");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> runtimeExceptionHandler(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Some thing unexpected happen in UserService");
    }
}
