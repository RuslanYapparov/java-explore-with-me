package ru.practicum.explore_with_me.stats_service.server_submodule.exception;

public class BadRequestParameterException extends RuntimeException {

    public BadRequestParameterException(String message) {
        super(message);
    }

}