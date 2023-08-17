package ru.practicum.explore_with_me.main_service.controller.for_all;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import ru.practicum.explore_with_me.main_service.model.rest_dto.event.HttpPublicGetAllRequestParamsHolder;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.EventRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.EventRestViewShort;
import ru.practicum.explore_with_me.main_service.service.EventService;
import ru.practicum.explore_with_me.main_service.util.StatsServiceIntegrator;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
public class PublicEventController {
    private final EventService eventService;
    private final StatsServiceIntegrator statsServiceIntegrator;

    @GetMapping("{event_id}")
    public EventRestView getEventById(@PathVariable(name = "event_id") long eventId, HttpServletRequest request) {
        log.debug("New public request to get event with id'{}' was received", eventId);
        EventRestView event = eventService.getEventById(eventId);
        statsServiceIntegrator.saveStatHitFromThisRequests(request.getRemoteAddr(), request.getRequestURI());
        return event;
    }

    @GetMapping
    public List<EventRestViewShort> getAllEventsByParameters(
            @RequestParam(name = "text", required = false) String text,
            @RequestParam(name = "categories", required = false) long[] categories,
            @RequestParam(name = "paid", required = false) Boolean paid,
            @RequestParam(name = "rangeStart", required = false) String rangeStart,
            @RequestParam(name = "rangeEnd", required = false) String rangeEnd,
            @RequestParam(name = "onlyAvailable", defaultValue = "false") boolean onlyAvailable,
            @RequestParam(name = "sort", defaultValue = "EVENT_DATE") String sort,
            @RequestParam(name = "from", defaultValue = "0") int from,
            @RequestParam(name = "size", defaultValue = "10") int size,
            HttpServletRequest request) {
        HttpPublicGetAllRequestParamsHolder httpParams = HttpPublicGetAllRequestParamsHolder.builder()
                .text(text)
                .categories(categories)
                .paid(paid)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .onlyAvailable(onlyAvailable)
                .sort(sort)
                .from(from)
                .size(size)
                .build();
        log.debug("New public request to get events with parameters '{}' was received", httpParams);
        List<EventRestViewShort> events = eventService.getAllEventsByParametersForAnyone(httpParams);
        statsServiceIntegrator.saveStatHitFromThisRequests(request.getRemoteAddr(), request.getRequestURI());
        return events;
    }

}
