package ru.practicum.explore_with_me.main_service.service;

import ru.practicum.explore_with_me.main_service.model.rest_dto.request.ModeratedRequestsRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.request.RequestRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.request.RequestRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.request.RequestStatusSetRestCommand;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.math.BigInteger;
import java.util.List;

public interface RequestService {

    RequestRestView saveNewRequest(@Valid RequestRestCommand requestRestCommand);

    List<RequestRestView> getAllRequestsOfUser(@Positive long userId);

    RequestRestView cancelRequestByRequester(@Positive long userId, @Positive BigInteger requestId);

    List<RequestRestView> getAllRequestsToEventForInitiator(@Positive long userId, @Positive long eventId);

    ModeratedRequestsRestView setStatusToRequestsByInitiator(@Positive long userId, @Positive long eventId,
                                                             @Valid RequestStatusSetRestCommand command);

}