package ru.practicum.explore_with_me.main_service.controller.for_authorized;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore_with_me.main_service.model.rest_dto.request.RequestRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.request.RequestRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.request.RequestStatusSetRestCommand;
import ru.practicum.explore_with_me.main_service.service.RequestService;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/users/{user_id}")
@RequiredArgsConstructor
@Slf4j
public class RequestController {
    private final RequestService requestService;

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public RequestRestView saveNewRequest(@PathVariable(name = "user_id") long userId,
                                          @RequestParam(name = "eventId") long eventId) {
        log.debug("New HTTP-request to save request from user with id'{}' to event with id'{}' was received",
                userId, eventId);
        return requestService.saveNewRequest(RequestRestCommand.builder()
                .requester(userId)
                .event(eventId)
                .build());
    }

    @GetMapping("/requests")
    public List<RequestRestView> getAllRequestsByUserId(@PathVariable(name = "user_id") long userId) {
        log.debug("New HTTP-request from user with id'{}' for list of all his requests was received. ", userId);
        return requestService.getAllRequestsOfUser(userId);
    }

    @GetMapping("/events/{event_id}/requests")
    public List<RequestRestView> getAllRequestsToEventForInitiator(@PathVariable(name = "user_id") long userId,
                                                                   @PathVariable(name = "event_id") long eventId) {
        log.debug("New HTTP-request from user with id'{}' for list of all requests for his event with id'{}' " +
                "was received", userId, eventId);
        return requestService.getAllRequestsToEventForInitiator(userId, eventId);
    }

    @PatchMapping("/requests/{request_id}/cancel")
    public RequestRestView cancelRequest(@PathVariable(name = "user_id") long userId,
                                         @PathVariable(name = "request_id") BigInteger requestId) {
        log.debug("New HTTP-request to cancel request with id'{}' from user with id'{}' was received", requestId, userId);
        return requestService.cancelRequestByRequester(userId, requestId);
    }

    @PatchMapping("/events/{event_id}/requests")
    public List<RequestRestView> setStatusToRequestsByInitiator(@PathVariable(name = "user_id") long userId,
                                                                @PathVariable(name = "event_id") long eventId,
                                                                @RequestBody RequestStatusSetRestCommand command) {
        log.debug("New HTTP-request from user with id'{}' to change status of some requests " +
                "for his event with id'{}' was received. Information about changes: {}", userId, eventId, command);
        return requestService.setStatusToRequestsByInitiator(userId, eventId, command);
    }

}