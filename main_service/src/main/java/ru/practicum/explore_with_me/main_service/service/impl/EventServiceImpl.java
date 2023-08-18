package ru.practicum.explore_with_me.main_service.service.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import ru.practicum.explore_with_me.main_service.exception.BadRequestBodyException;
import ru.practicum.explore_with_me.main_service.exception.BadRequestParameterException;
import ru.practicum.explore_with_me.main_service.exception.ObjectNotFoundException;
import ru.practicum.explore_with_me.main_service.exception.StatsServiceProblemException;
import ru.practicum.explore_with_me.main_service.mapper.impl.EventMapper;
import ru.practicum.explore_with_me.main_service.model.db_entities.CategoryEntity;
import ru.practicum.explore_with_me.main_service.model.db_entities.UserEntity;
import ru.practicum.explore_with_me.main_service.model.db_entities.event.EventEntity;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.event.Event;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.event.EventState;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.params_holder.JpaAdminGetAllQueryParamsHolder;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.params_holder.JpaPublicGetAllQueryParamsHolder;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.HttpPublicGetAllRequestParamsHolder;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.*;
import ru.practicum.explore_with_me.main_service.repository.CategoryRepository;
import ru.practicum.explore_with_me.main_service.repository.EventRepository;
import ru.practicum.explore_with_me.main_service.repository.UserRepository;
import ru.practicum.explore_with_me.main_service.service.EventService;
import ru.practicum.explore_with_me.main_service.util.QueryDslExpressionCreator;
import ru.practicum.explore_with_me.main_service.util.MethodParameterValidator;
import ru.practicum.explore_with_me.main_service.util.StatsServiceIntegrator;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.EwmConstants;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Validated
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;
    private final StatsServiceIntegrator statsServiceIntegrator;

    @Override
    public EventRestView saveNewEvent(@Positive long userId, @Valid EventRestCommand eventRestCommand) {
        UserEntity initiator = getInitiatorIfExists(userId);
        CategoryEntity category = getCategoryIfExists(eventRestCommand.getCategory());
        eventRestCommand = MethodParameterValidator
                .getEventRestCommandCheckedForSpecificLogic(eventRestCommand, null);
        Event event = eventMapper.fromRestCommand(eventRestCommand).toBuilder()
                .state(EventState.PENDING)
                .build();
        EventEntity eventEntity = eventMapper.toDbEntity(event);
        eventEntity.setInitiator(initiator);
        eventEntity.setCategory(category);
        eventEntity.setCreatedOn(LocalDateTime.now());
        eventEntity = eventRepository.save(eventEntity);
        event = eventMapper.fromDbEntity(eventEntity);
        log.info("User with id'{}' created new event '{}'", userId, event);
        return eventMapper.toRestView(event);
    }

    @Override
    public EventRestView getEventById(@Positive long eventId) {
        EventEntity eventEntity = getEventEntityIfExists(eventId);
        if (!EventState.PUBLISHED.name().equals(eventEntity.getState())) {
            throw new ObjectNotFoundException("Requested event with id'" + eventId + "' not published");
        }

        Event event = eventMapper.fromDbEntity(eventEntity).toBuilder()
                .views(statsServiceIntegrator.getViewsForOneUri("/events/" + eventId))
                .build();
        log.info("Event {} was sent to client", event);
        return eventMapper.toRestView(event);
    }

    @Override
    public EventRestView getEventOfUserById(@Positive long userId, @Positive long eventId) {
        UserEntity initiator = getInitiatorIfExists(userId);
        EventEntity eventEntity = getEventEntityIfExists(eventId);
        if (!eventEntity.getInitiator().equals(initiator)) {
            throw new BadRequestParameterException(String.format("User with id '%d' is not the initiator of " +
                    "event with id'%d' and can't receive information about it by private API", eventId, userId));
        }

        Event event = eventMapper.fromDbEntity(eventEntity).toBuilder()
                .views(statsServiceIntegrator.getViewsForOneUri("/events/" + eventId))
                .build();
        log.info("{} was sent to its initiator with id'{}'", event, userId);
        return eventMapper.toRestView(event);
    }

    @Override
    public List<EventRestViewShort> getAllEventsByParametersForAnyone(
            @Valid HttpPublicGetAllRequestParamsHolder httpParams) {
        JpaPublicGetAllQueryParamsHolder paramsHolder = MethodParameterValidator
                .getValidJpaQueryParamsFromHttpRequest(httpParams);
        BooleanExpression preparedConditions = QueryDslExpressionCreator.prepareConditionsForQuery(paramsHolder);
        Sort sort = Sort.by(Sort.Direction.ASC, "eventDate", "id");
        Pageable page = PageRequest.of(paramsHolder.getFrom(), paramsHolder.getSize(), sort);
        List<EventEntity> eventsEntities = eventRepository.findAll(preparedConditions, page).getContent();
        if (eventsEntities.size() == 0) {
            return Collections.emptyList();
        }

        List<Event> events = mapEventEntitiesToEventsWithViews(eventsEntities);
        log.info("List of events with size '{}' requested with parameters '{}' was sent to client",
                events.size(), httpParams);
        switch (paramsHolder.getSort()) {
            case EVENT_DATE:
                return events.stream()
                        .map(eventMapper::mapEventRestViewShortFromEvent)
                        .collect(Collectors.toList());
            case VIEWS:
                return events.stream()
                        .sorted(Comparator.comparingLong(Event::getViews).reversed())
                        .map(eventMapper::mapEventRestViewShortFromEvent)
                        .collect(Collectors.toList());
            default:
                throw new IllegalArgumentException("There is no sort logic for parameter SortBy."
                        + paramsHolder.getSort());
        }
    }

    @Override
    public List<EventRestViewShort> getAllEventsByUserId(@Positive long userId,
                                                         @PositiveOrZero int from,
                                                         @Positive int size) {
        Pageable page = PageRequest.of(from, size, Sort.by(Sort.Direction.ASC, "id"));
        Page<EventEntity> eventEntitiesPage = eventRepository.findAllByInitiatorId(userId, page);
        List<Event> events = mapEventEntitiesToEventsWithViews(eventEntitiesPage.getContent());
        log.info("List of {} events requested by user with id'{}' was sent to client. Page parameters: " +
                        "from='{}', size='{}'", events.size(), userId, from, size);
        return events.stream()
                .map(eventMapper::mapEventRestViewShortFromEvent)
                .collect(Collectors.toList());
    }

    @Override
    public List<EventRestView> getAllEventsByParametersForAdmin(@Valid HttpAdminGetAllRequestParamsHolder httpParams) {
        JpaAdminGetAllQueryParamsHolder paramsHolder = MethodParameterValidator
                .getValidJpaQueryParamsFromHttpRequest(httpParams);
        BooleanExpression preparedConditions = QueryDslExpressionCreator.prepareConditionsForQuery(paramsHolder);
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        Pageable page = PageRequest.of(paramsHolder.getFrom(), paramsHolder.getSize(), sort);
        List<EventEntity> eventsEntities = eventRepository.findAll(preparedConditions, page).getContent();
        if (eventsEntities.size() == 0) {
            return Collections.emptyList();
        }
        List<Event> events = mapEventEntitiesToEventsWithViews(eventsEntities);
        log.info("List of events with size '{}' requested with parameters '{}' was sent to client",
                events.size(), httpParams);
        return events.stream()
                .map(eventMapper::toRestView)
                .collect(Collectors.toList());
    }

    @Override
    public EventRestView updateEventFromInitiator(@Positive long userId,
                                                  @Positive long eventId,
                                                  EventRestCommand eventRestCommand) {
        UserEntity initiator = getInitiatorIfExists(userId);
        EventEntity eventEntity = getEventEntityIfExists(eventId);
        if (!eventEntity.getInitiator().equals(initiator)) {
            throw new BadRequestBodyException("Only initiator can make changes in his event");
        }

        eventRestCommand = MethodParameterValidator
                .getEventRestCommandCheckedForSpecificLogic(eventRestCommand, false);
        eventEntity = updateEventEntityInformationFromRestCommand(eventEntity, eventRestCommand, false);
        eventEntity = eventRepository.save(eventEntity);
        Event event = eventMapper.fromDbEntity(eventEntity).toBuilder()
                .views(statsServiceIntegrator.getViewsForOneUri("/events/" + eventId))
                .build();
        log.info("User with id'{}' updated his event with id'{}'. Updated {}", userId, eventId, event);
        return eventMapper.toRestView(event);
    }

    @Override
    public EventRestView updateEventFromAdmin(@Positive long eventId, EventRestCommand eventRestCommand) {
        EventEntity eventEntity = getEventEntityIfExists(eventId);
        eventRestCommand = MethodParameterValidator
                .getEventRestCommandCheckedForSpecificLogic(eventRestCommand, true);
        eventEntity = updateEventEntityInformationFromRestCommand(eventEntity, eventRestCommand, true);
        eventEntity = eventRepository.save(eventEntity);
        Event event = eventMapper.fromDbEntity(eventEntity).toBuilder()
                .views(statsServiceIntegrator.getViewsForOneUri("/events/" + eventId))
                .build();
        log.info("Admin updated event with id'{}'. Updated {}", eventId, event);
        return eventMapper.toRestView(event);
    }

    private EventEntity getEventEntityIfExists(long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() ->  new ObjectNotFoundException(
                "Failed to save/update/get event: event with id'" + eventId + "' was not saved"));
    }

    private UserEntity getInitiatorIfExists(long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ObjectNotFoundException(
                "Failed to save/update/get event: user (initiator) with id'" + userId + "' was not saved"));
    }

    private CategoryEntity getCategoryIfExists(long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(() -> new ObjectNotFoundException(
                "Failed to save/update/get event: specified category with id'" + categoryId + "' was not saved"));
    }

    private List<Event> mapEventEntitiesToEventsWithViews(List<EventEntity> entities) {
        String[] uris = entities.stream()
                .map(EventEntity::getId)
                .map(entityId -> "/events/" + entityId)
                .collect(Collectors.toList()).toArray(new String[] {});
        Map<Long, Long> viewsStatistics = new HashMap<>();
        Arrays.stream(statsServiceIntegrator.getUriStatsFromService(uris))
                .filter(uriStat -> !(uriStat.getUri().equals("/events")))
                .forEach(uriStat -> {
                    long eventId;
                    try {
                        eventId = Long.parseLong(uriStat.getUri().substring(8));
                    } catch (NumberFormatException exception) {
                        throw new StatsServiceProblemException("Unsupported URI format found in response from " +
                                "Stats_service: " + uriStat.getUri());
                    }
                    viewsStatistics.put(eventId, uriStat.getHits());
                });
        return entities.stream()
                .map(eventEntity -> eventMapper.fromDbEntity(eventEntity).toBuilder()
                                .views(viewsStatistics.getOrDefault(eventEntity.getId(), 0L))
                                .build())
                .collect(Collectors.toList());
    }

    private EventEntity updateEventEntityInformationFromRestCommand(EventEntity eventEntity,
                                                                    EventRestCommand eventRestCommand,
                                                                    boolean afterModeration) {
        StateAction stateAction = eventRestCommand.getStateAction() == null ? null :
                StateAction.valueOf(eventRestCommand.getStateAction());
        EventState state = EventState.valueOf(eventEntity.getState());
        if (afterModeration) {
            if (!EventState.PENDING.equals(state)) {
                throw new BadRequestBodyException("The event for updating by admin is not in WAITING state");
            }
            return fillEventEntityFieldsFromEventRestCommand(eventEntity, eventRestCommand, true);
        }
        switch (state) {
            case PUBLISHED:
                throw new BadRequestBodyException("Only pending or canceled events can be changed");
                case PENDING:
                if (stateAction != null && !StateAction.CANCEL_REVIEW.equals(stateAction)) {
                    throw new BadRequestBodyException(String.format("Unsupported operation '%S' " +
                            "for event in state '%S'", stateAction, state));
                }
                return fillEventEntityFieldsFromEventRestCommand(eventEntity, eventRestCommand, false);
            case CANCELED:
                if (stateAction != null && !StateAction.SEND_TO_REVIEW.equals(stateAction)) {
                    throw new BadRequestBodyException(String.format("Unsupported operation '%S' " +
                            "for event in state '%S'", stateAction, state));
                }
                return fillEventEntityFieldsFromEventRestCommand(eventEntity, eventRestCommand, false);
            default:
                throw new IllegalArgumentException("There is no service logic for parameter EventState."
                        + eventEntity.getState());
        }
    }

    private EventEntity fillEventEntityFieldsFromEventRestCommand(EventEntity eventEntity,
                                                                  EventRestCommand eventRestCommand,
                                                                  boolean afterModeration) {
        long categoryId = eventRestCommand.getCategory();
        String title = eventRestCommand.getTitle();
        String annotation = eventRestCommand.getAnnotation();
        String description = eventRestCommand.getDescription();
        String eventDate = eventRestCommand.getEventDate();
        GeoLocation location = eventRestCommand.getLocation();
        Boolean paid = eventRestCommand.getPaid();
        Boolean requestModeration = eventRestCommand.getRequestModeration();
        Integer participantLimit = eventRestCommand.getParticipantLimit();
        StateAction stateAction = eventRestCommand.getStateAction() == null ? null :
                StateAction.valueOf(eventRestCommand.getStateAction());
        EventState state = considerState(EventState.valueOf(eventEntity.getState()), stateAction, afterModeration);

        CategoryEntity categoryEntity = categoryId == 0L ? eventEntity.getCategory() : getCategoryIfExists(categoryId);
        int confirmedRequests = eventEntity.getConfirmedRequests();
        if (participantLimit != null && participantLimit != 0) {
            if (participantLimit <= confirmedRequests) {
                throw new BadRequestBodyException(String.format("Cannot reduce limit of participants to value '%d': " +
                        "there are '%d' confirmed requests for this event", participantLimit, confirmedRequests));
            }
        }
        eventEntity.setCategory(categoryId == 0L ? eventEntity.getCategory() : categoryEntity);
        eventEntity.setTitle(title == null ? eventEntity.getTitle() : title);
        eventEntity.setAnnotation(annotation == null ? eventEntity.getAnnotation() : annotation);
        eventEntity.setDescription(description == null ? eventEntity.getDescription() : description);
        eventEntity.setEventDate(eventDate == null ? eventEntity.getEventDate() :
                LocalDateTime.parse(eventDate, EwmConstants.FORMATTER));
        eventEntity.setLocation(location == null ? eventEntity.getLocation() :
                eventMapper.mapGeoLocationToGeoLocationEntity(location));
        eventEntity.setPaid(paid == null ? eventEntity.isPaid() : paid);
        eventEntity.setRequestModeration(requestModeration == null ? eventEntity.isRequestModeration() :
                        requestModeration);
        eventEntity.setParticipantLimit(participantLimit == null ? eventEntity.getParticipantLimit() :
                        participantLimit);
        eventEntity.setState(state.name());
        if (afterModeration && EventState.PUBLISHED.equals(state)) {
            eventEntity.setPublishedOn(LocalDateTime.now());
        }
        return eventEntity;
    }

    private EventState considerState(EventState state, StateAction stateAction, boolean afterModeration) {
        if (afterModeration) {
            if (stateAction == null) {
                return state;
            } else if (StateAction.PUBLISH_EVENT.equals(stateAction)) {
                return EventState.PUBLISHED;
            } else if (StateAction.REJECT_EVENT.equals(stateAction)) {
                return EventState.CANCELED;
            } else {
                throw new BadRequestBodyException("Unsupported operation '" + stateAction
                        + "' for updating event after moderation");
            }
        } else {
            if (stateAction == null) {
                return state;
            } else {
                return stateAction == StateAction.SEND_TO_REVIEW ? EventState.PENDING : EventState.CANCELED;
            }
        }
    }

}