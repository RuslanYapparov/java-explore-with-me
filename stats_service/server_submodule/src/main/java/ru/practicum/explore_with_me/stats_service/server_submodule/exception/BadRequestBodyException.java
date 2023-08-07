package ru.practicum.explore_with_me.stats_service.server_submodule.exception;

public class BadRequestBodyException extends RuntimeException {

    public BadRequestBodyException(String message) {
        super(message);
    }

}