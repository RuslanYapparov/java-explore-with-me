package ru.practicum.explore_with_me.main_service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.practicum.explore_with_me.main_service.exception.BadRequestBodyException;
import ru.practicum.explore_with_me.main_service.exception.BadRequestParameterException;
import ru.practicum.explore_with_me.main_service.exception.ObjectNotFoundException;
import ru.practicum.explore_with_me.main_service.mapper.impl.RequestMapper;
import ru.practicum.explore_with_me.main_service.model.db_entities.RequestEntity;
import ru.practicum.explore_with_me.main_service.model.db_entities.UserEntity;
import ru.practicum.explore_with_me.main_service.model.db_entities.event.EventEntity;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.event.EventState;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.request.Request;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.request.RequestStatus;
import ru.practicum.explore_with_me.main_service.model.rest_dto.request.ModeratedRequestsRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.request.RequestRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.request.RequestRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.request.RequestStatusSetRestCommand;
import ru.practicum.explore_with_me.main_service.repository.EventRepository;
import ru.practicum.explore_with_me.main_service.repository.RequestRepository;
import ru.practicum.explore_with_me.main_service.repository.UserRepository;
import ru.practicum.explore_with_me.main_service.service.RequestService;
import ru.practicum.explore_with_me.main_service.util.MethodParameterValidator;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestMapper requestMapper;

    @Override
    public RequestRestView saveNewRequest(@Valid RequestRestCommand requestRestCommand) {
        UserEntity requester = getUserEntityIfExists(requestRestCommand.getRequester());
        EventEntity event = getCheckedEventEntityIfExists(requestRestCommand.getEvent(), true);
        if (requester.equals(event.getInitiator())) {
            throw new BadRequestBodyException("Failed to save request: initiator can't request participation " +
                    "in his event");
        }
        Request request = requestMapper.fromRestCommand(requestRestCommand).toBuilder()
                .status(event.getParticipantLimit() != 0 ? RequestStatus.PENDING : RequestStatus.CONFIRMED)
                .build();
        RequestEntity requestEntity = requestMapper.toDbEntity(request);
        requestEntity.setRequester(requester);
        requestEntity.setEvent(event);
        requestEntity.setCreatedOn(LocalDateTime.now());
        requestEntity = requestRepository.save(requestEntity);
        request = requestMapper.fromDbEntity(requestEntity);
        log.info("New '{}' was saved", request);
        return requestMapper.toRestView(request);
    }

    @Override
    public List<RequestRestView> getAllRequestsOfUser(@Positive long userId) {
        getUserEntityIfExists(userId);
        List<RequestEntity> requests = requestRepository.findAllByRequesterId(userId);
        log.info("List of {} requests of user with id'{}' was sent to client", requests.size(), userId);
        return mapListOfRequestRestViewsFromRequestEntities(requests);
    }

    @Override
    public RequestRestView cancelRequestByRequester(@Positive long userId, @Positive BigInteger requestId) {
        UserEntity requester = getUserEntityIfExists(userId);
        RequestEntity requestEntity = getRequestIfExists(requestId);
        if (!requester.equals(requestEntity.getRequester())) {
            throw new BadRequestParameterException("Failed to cancel request: only requester can cancel his request");
        }
        switch (RequestStatus.valueOf(requestEntity.getStatus())) {
            case CANCELED:
            case REJECTED:
                throw new BadRequestBodyException("Failed to cancel request: request already is in " +
                        "CANCELLED or REJECTED status");
            case PENDING:
                break;
            case CONFIRMED:
                EventEntity event = requestEntity.getEvent();
                event.setConfirmedRequests(event.getConfirmedRequests() - 1);
                eventRepository.save(event);
        }
        requestEntity.setStatus(RequestStatus.CANCELED.name());
        requestEntity = requestRepository.save(requestEntity);
        Request request = requestMapper.fromDbEntity(requestEntity);
        log.info("{}", request);
        return requestMapper.toRestView(request);
    }

    @Override
    public List<RequestRestView> getAllRequestsToEventForInitiator(@Positive long userId, @Positive long eventId) {
        UserEntity initiator = getUserEntityIfExists(userId);
        EventEntity event = getCheckedEventEntityIfExists(eventId, false);
        if (!initiator.equals(event.getInitiator())) {
            throw new BadRequestParameterException(String.format("User with id'%d' is not initiator of event " +
                    "with id'%d'", userId, eventId));
        }
        List<RequestEntity> requests = requestRepository.findAllByEventId(eventId);
        log.info("List of {} requests for event with id'{}' was sent to client", requests.size(), eventId);
        return mapListOfRequestRestViewsFromRequestEntities(requests);
    }

    @Override
    public ModeratedRequestsRestView setStatusToRequestsByInitiator(@Positive long userId, @Positive long eventId,
                                                                    @Valid RequestStatusSetRestCommand command) {
        MethodParameterValidator.checkStatusCommandForSpecificLogic(command);
        UserEntity initiator = getUserEntityIfExists(userId);
        EventEntity event = getCheckedEventEntityIfExists(eventId, false);
        if (!EventState.PUBLISHED.name().equals(event.getState())) {
            throw new UnsupportedOperationException("Failed to change status of requests for event with id'"
                    + eventId + "': event is not in PUBLISHED state");
        }
        if (!initiator.equals(event.getInitiator())) {
            throw new BadRequestParameterException(String.format("User with id'%d' is not initiator of event " +
                    "with id'%d'", userId, eventId));
        }
        List<RequestEntity> requests = getListOfUpdatedRequestEntities(event, command);
        RequestStatus status = RequestStatus.valueOf(command.getStatus());

        List<RequestEntity> allRequestsOfEvent = requestRepository.findAllByEventId(eventId);
        log.info("Status of requests with ids'" +
                requests.stream().map(RequestEntity::getId).collect(Collectors.toList()) + "' was changed to '" +
                status + "'");
        return requestMapper.mapModeratedRequestsRestViewFromListOfRequests(
                mapListOfRequestRestViewsFromRequestEntities(allRequestsOfEvent));
    }

    private EventEntity getCheckedEventEntityIfExists(long eventId, boolean isRequestSaving) {
        EventEntity event = eventRepository.findById(eventId).orElseThrow(() ->  new ObjectNotFoundException(
                "Failed to save/update/get request: event with id'" + eventId + "' was not saved"));
        return isRequestSaving ? MethodParameterValidator.getValidEventEntityToSaveRequest(event) : event;
    }

    private UserEntity getUserEntityIfExists(long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ObjectNotFoundException(
                "Failed to save/update/get request: user (initiator) with id'" + userId + "' was not saved"));
    }

    private RequestEntity getRequestIfExists(BigInteger requestId) {
        return requestRepository.findById(requestId).orElseThrow(() -> new ObjectNotFoundException(
                "Failed to save/update/get request: request with id'" + requestId + "' was not saved"));
    }

    private List<RequestRestView> mapListOfRequestRestViewsFromRequestEntities(List<RequestEntity> requestEntities) {
        return requestEntities.stream()
                .map(requestEntity -> requestMapper.toRestView(requestMapper.fromDbEntity(requestEntity)))
                .collect(Collectors.toList());
    }

    private List<RequestEntity> getListOfUpdatedRequestEntities(
            EventEntity event, RequestStatusSetRestCommand command) {
        List<RequestEntity> requests = new ArrayList<>();
        int participantLimit = event.getParticipantLimit();
        int confirmedRequests = event.getConfirmedRequests();
        BigInteger[] requestIds = command.getRequestIds();
        RequestStatus status = RequestStatus.valueOf(command.getStatus());
        if (participantLimit != 0 && event.isRequestModeration()) {
            requests = requestRepository.findAllByIdIn(requestIds);
            if (RequestStatus.CONFIRMED.equals(status) && confirmedRequests + requests.size() > participantLimit) {
                throw new BadRequestBodyException(String.format("Too many requests to confirm: participant limit " +
                        "is '%d', number of already confirmed requests is '%d', number of new confirmed requests " +
                        "is '%d'", participantLimit, confirmedRequests, requests.size()));
            }
            requests.forEach(requestEntity -> {
                if (RequestStatus.REJECTED.equals(status)) {
                    if (RequestStatus.CONFIRMED.name().equals(requestEntity.getStatus())) {
                        throw new BadRequestBodyException("Failed to cancel request with id'" + requestEntity.getId() +
                                "': request was confirmed earlier");
                    }
                }
                requestEntity.setStatus(status.name());
            });
            requestRepository.saveAll(requests);
            if (RequestStatus.CONFIRMED.equals(status)) {
                event.setConfirmedRequests(confirmedRequests + requests.size());
                eventRepository.save(event);
            }
        }
        return requests;
    }

}