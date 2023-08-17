package ru.practicum.explore_with_me.main_service.service;

import ru.practicum.explore_with_me.main_service.model.rest_dto.event.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

public interface EventService {

    EventRestView saveNewEvent(@Positive long userId, @Valid EventRestCommand eventRestCommand);

    EventRestView getEventById(@Positive long eventId);

    EventRestView getEventOfUserById(@Positive long userId, @Positive long eventId);

    List<EventRestViewShort> getAllEventsByParametersForAnyone(@Valid HttpPublicGetAllRequestParamsHolder httpParams);

    List<EventRestView> getAllEventsByParametersForAdmin(@Valid HttpAdminGetAllRequestParamsHolder httpParams);

    List<EventRestViewShort> getAllEventsByUserId(@Positive long userId, @PositiveOrZero int from, @Positive int size);

    EventRestView updateEventFromInitiator(@Positive long userId, @Positive long eventId,
                                           EventRestCommand eventRestCommand);

    EventRestView updateEventFromAdmin(@Positive long eventId, EventRestCommand eventRestCommand);

}