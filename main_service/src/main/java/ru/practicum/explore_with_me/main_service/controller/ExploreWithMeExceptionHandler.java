package ru.practicum.explore_with_me.main_service.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import javax.validation.ConstraintViolationException;

import ru.practicum.explore_with_me.main_service.exception.ObjectModificationException;
import ru.practicum.explore_with_me.main_service.exception.BadRequestParameterException;
import ru.practicum.explore_with_me.main_service.exception.ObjectNotFoundException;
import ru.practicum.explore_with_me.main_service.exception.StatsServiceProblemException;
import ru.practicum.explore_with_me.main_service.model.rest_dto.ErrorResponse;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class ExploreWithMeExceptionHandler {

    @ExceptionHandler(ObjectNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleObjectNotFoundInStorageException(ObjectNotFoundException exception) {
        log.warn(exception.getMessage());
        return ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND)
                .reason("There is no saved object with specified id.")
                .message(exception.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(BadRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequestParameterException(BadRequestParameterException exception) {
        log.warn(exception.getMessage());
        return ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST)
                .reason("Http request was made with error(s).")
                .message(exception.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(ObjectModificationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleObjectModificationException(ObjectModificationException exception) {
        log.warn(exception.getMessage());
        return ErrorResponse.builder()
                .status(HttpStatus.FORBIDDEN) // В соответствии со спецификацией, в поле статус должно быть это значение
                // При этом код http-ответа должен быть 409
                .reason("For the requested operation the conditions are not met.")
                .message(exception.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(StatsServiceProblemException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleStatsServiceProblemException(StatsServiceProblemException exception) {
        log.warn(exception.getMessage());
        return ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .reason("There is problem with getting information from Stats_service (see message).")
                .message(exception.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDataIntegrityViolationException(DataIntegrityViolationException exception) {
        log.warn(exception.getMessage());
        return ErrorResponse.builder()
                .status(HttpStatus.CONFLICT)
                .reason("Integrity constraint has been violated (see message).")
                .message("Failed to create/update object data: " + exception.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException exception) {
        String name = exception.getName();
        Object value = exception.getValue();
        Class<?> requiredType = exception.getRequiredType();
        String exceptionMessage = exception.getMessage();
        String message;
        if (name == null || value == null || requiredType == null || exceptionMessage == null) {
            message = "One of the parameters of method could not be converted to type required type";
        } else {
            message = String.format("The parameter '%s' of value '%s' could not be converted to type '%s'. " +
                    "Cause: %s", name, value, requiredType, exceptionMessage);
        }
        log.warn(message);
        return ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST)
                .reason("Incorrectly made request: type of one or more parameters is not supported (see message).")
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleHttpMethodNotReadableException(HttpMessageNotReadableException exception) {
        log.warn(HttpMessageNotReadableException.class + ": " + exception.getMessage());
        return ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST)
                .reason("Failed to read request body (content is incorrect).")
                .message(exception.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolationException(ConstraintViolationException exception) {
        log.warn(exception.toString());
        return ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST)
                .reason("Incorrectly made request: constraint for parameter was violated (see message).")
                .message(exception.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAnotherUnhandledException(RuntimeException exception) {
        String message = exception.getClass().toString() + ": " + exception.getMessage();
        log.warn(message);
        return ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .reason("Unexpected exception during application work. Please inform the developers.")
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

}