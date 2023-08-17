package ru.practicum.explore_with_me.main_service.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import ru.practicum.explore_with_me.main_service.model.rest_dto.event.EventRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.EventRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.HttpAdminGetAllRequestParamsHolder;
import ru.practicum.explore_with_me.main_service.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/admin/events")
@Slf4j
@RequiredArgsConstructor
public class AdminEventsController {
    private final EventService eventService;

    @GetMapping
    public List<EventRestView> saveNewUser(
            @RequestParam(name = "users", required = false) long[] users,
            @RequestParam(name = "states", required = false) String[] states,
            @RequestParam(name = "categories", required = false) long[] categories,
            @RequestParam(name = "rangeStart", required = false) String rangeStart,
            @RequestParam(name = "rangeEnd", required = false) String rangeEnd,
            @RequestParam(name = "from", defaultValue = "0") int from,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        HttpAdminGetAllRequestParamsHolder httpParams = new HttpAdminGetAllRequestParamsHolder(users,
                states,
                categories,
                rangeStart,
                rangeEnd,
                from,
                size);
        log.debug("New admin request to get events with parameters '{}' was received", httpParams);
        return eventService.getAllEventsByParametersForAdmin(httpParams);
    }

    @PatchMapping("{event_id}")
    public EventRestView updateEvent(@PathVariable(name = "event_id") long eventId,
                                     @RequestBody EventRestCommand eventRestCommand) {
        log.debug("New request to update event with id'{}' from admin was received. " +
                "Information to update: {}", eventId, eventRestCommand);
        return eventService.updateEventFromAdmin(eventId, eventRestCommand);
    }

}
