package ru.practicum.explore_with_me.stats_service.server_submodule.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;

@RestControllerAdvice
@Slf4j
public class StatsServerExceptionHandler {

    @ExceptionHandler(BadRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequestParameterException(BadRequestParameterException exception) {
        log.warn(exception.getMessage());
        return exception.getMessage();
    }

    @ExceptionHandler(BadRequestBodyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequestBodyException(BadRequestBodyException exception) {
        log.warn(exception.getMessage());
        return exception.getMessage();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleConstraintViolationException(ConstraintViolationException exception) {
        log.warn(exception.getMessage());
        return exception.getMessage();
    }

}