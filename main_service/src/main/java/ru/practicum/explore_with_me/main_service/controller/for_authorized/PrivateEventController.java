package ru.practicum.explore_with_me.main_service.controller.for_authorized;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ru.practicum.explore_with_me.main_service.model.rest_dto.event.EventRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.EventRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.EventRestViewShort;
import ru.practicum.explore_with_me.main_service.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/users/{user_id}/events")
@RequiredArgsConstructor
@Slf4j
public class PrivateEventController {
    private final EventService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventRestView saveNewEvent(@PathVariable(name = "user_id") long userId,
                                      @RequestBody EventRestCommand eventRestCommand) {
        log.debug("New request to save event '{}' from user with id'{}' was received", eventRestCommand, userId);
        return eventService.saveNewEvent(userId, eventRestCommand);
    }

    @GetMapping
    public List<EventRestViewShort> getAllEventsByUserId(@PathVariable(name = "user_id") long userId,
                                                         @RequestParam(name = "from", defaultValue = "0") int from,
                                                         @RequestParam(name = "size", defaultValue = "10") int size) {
        log.debug("New request from user with id'{}' for page of his events with size '{}' " +
                "from index '{}' was received. ", userId, size, from);
        return eventService.getAllEventsByUserId(userId, from, size);
    }

    @GetMapping("{event_id}")
    public EventRestView getEventOfUserById(@PathVariable(name = "user_id") long userId,
                                                       @PathVariable(name = "event_id") long eventId) {
        log.debug("New request from user with id'{}' for his event with id'{}' was received", userId, eventId);
        return eventService.getEventOfUserById(userId, eventId);
    }

    @PatchMapping("{event_id}")
    public EventRestView updateEvent(@PathVariable(name = "user_id") long userId,
                                     @PathVariable(name = "event_id") long eventId,
                                     @RequestBody EventRestCommand eventRestCommand) {
        log.debug("New request to update event with id'{}' from user with id'{}' was received. " +
                "Information to update: {}", eventId, userId, eventRestCommand);
        return eventService.updateEventFromInitiator(userId, eventId, eventRestCommand);
    }

}