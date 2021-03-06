package com.example.prjava.project.exception;

import com.example.prjava.project.dto.ResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseDto<?> illegalArgumentException(IllegalArgumentException e){
        log.error("IllegalArgumentException",e);
        return ResponseDto.builder()
                .ok(false)
                .message(e.getMessage())
                .build();
    }    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseDto<?> methodArgumentNotValidException(MethodArgumentNotValidException e){
        log.error("IllegalArgumentException",e);
        return ResponseDto.builder()
                .ok(false)
                .message(e.getBindingResult().getAllErrors().get(0).getDefaultMessage())
                .build();
    }

}