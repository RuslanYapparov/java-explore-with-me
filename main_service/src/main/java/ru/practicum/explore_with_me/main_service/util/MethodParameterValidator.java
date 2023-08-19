package ru.practicum.explore_with_me.main_service.util;

import lombok.experimental.UtilityClass;

import ru.practicum.explore_with_me.main_service.exception.BadRequestBodyException;
import ru.practicum.explore_with_me.main_service.exception.BadRequestParameterException;
import ru.practicum.explore_with_me.main_service.model.db_entities.event.EventEntity;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.event.EventState;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.params_holder.JpaAdminGetAllQueryParamsHolder;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.params_holder.JpaPublicGetAllQueryParamsHolder;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.params_holder.SortBy;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.request.RequestStatus;
import ru.practicum.explore_with_me.main_service.model.rest_dto.compilation.CompilationRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.HttpAdminGetAllRequestParamsHolder;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.HttpPublicGetAllRequestParamsHolder;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.EventRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.StateAction;
import ru.practicum.explore_with_me.main_service.model.rest_dto.request.RequestStatusSetRestCommand;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.EwmConstants;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Set;

@UtilityClass
public class MethodParameterValidator {

    public static EventRestCommand getEventRestCommandCheckedForSpecificLogic(EventRestCommand eventRestCommand,
                                                                              Boolean isUpdating) {
        checkStringField(eventRestCommand.getTitle(), "title", 3, 120);
        checkStringField(eventRestCommand.getAnnotation(), "annotation", 20, 2000);
        checkStringField(eventRestCommand.getDescription(), "description", 20, 7000);
        checkDateTimeFromStringForSpecificLogic(eventRestCommand.getEventDate(),
                (isUpdating != null && isUpdating));
        Integer participantLimit = eventRestCommand.getParticipantLimit();
        String stateAction = eventRestCommand.getStateAction();

        if (participantLimit != null && participantLimit < 0) {
            throw new BadRequestBodyException("Limit of participants cannot be less than 0");
        }
        if (stateAction != null) {
            try {
                StateAction.valueOf(stateAction);
            } catch (IllegalArgumentException exception) {
                throw new BadRequestBodyException(String.format("Failed to update event: stateAction must be one " +
                        "of '%s', but was '%s'", Arrays.toString(StateAction.values()), stateAction));
            }
        }
        if (isUpdating == null) {
            Boolean paid = eventRestCommand.getPaid();
            Boolean requestModeration = eventRestCommand.getRequestModeration();
            return eventRestCommand.toBuilder()
                    .paid(paid != null ? paid : false)
                    .requestModeration(requestModeration != null ? requestModeration : true)
                    .participantLimit(participantLimit != null ? participantLimit : 0)
                    .build();
        }
        return eventRestCommand;
    }

    public static JpaPublicGetAllQueryParamsHolder getValidJpaQueryParamsFromHttpRequest(
            HttpPublicGetAllRequestParamsHolder httpParams) {
        LocalDateTime rangeStart = (httpParams.getRangeStart() == null) ? LocalDateTime.now() :
                parseDateTimeFromHttpParams(httpParams.getRangeStart());
        LocalDateTime rangeEnd = parseDateTimeFromHttpParams(httpParams.getRangeEnd());
        long[] categories = httpParams.getCategories();
        SortBy sort;

        compareRangeStartAndEnd(rangeStart, rangeEnd);
        if (categories != null) {
            checkArrayOfLongsForNegativeValues(categories, "category");
        }
        try {
            sort = SortBy.valueOf(httpParams.getSort());
        } catch (IllegalArgumentException exception) {
            throw new BadRequestParameterException("Wrong method parameter: " +
                    "requested sort option in unsupported format");
        }

        return JpaPublicGetAllQueryParamsHolder.builder()
                .text(httpParams.getText())
                .categories(categories)
                .paid(httpParams.getPaid())
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .onlyAvailable(httpParams.isOnlyAvailable())
                .sort(sort)
                .from(httpParams.getFrom())
                .size(httpParams.getSize())
                .build();
    }

    public static JpaAdminGetAllQueryParamsHolder getValidJpaQueryParamsFromHttpRequest(
            HttpAdminGetAllRequestParamsHolder httpParams) {
        LocalDateTime rangeStart = (httpParams.getRangeStart() == null) ? LocalDateTime.now() :
                parseDateTimeFromHttpParams(httpParams.getRangeStart());
        LocalDateTime rangeEnd = parseDateTimeFromHttpParams(httpParams.getRangeEnd());
        long[] users = httpParams.getUsers();
        long[] categories = httpParams.getCategories();
        String[] states = httpParams.getStates();

        compareRangeStartAndEnd(rangeStart, rangeEnd);
        if (users != null) {
            checkArrayOfLongsForNegativeValues(users, "user");
        }
        if (categories != null) {
            checkArrayOfLongsForNegativeValues(categories, "category");
        }
        if (states != null) {
            Arrays.stream(states).forEach(state -> {
                try {
                    EventState.valueOf(state);
                } catch (IllegalArgumentException exception) {
                    throw new BadRequestParameterException("There is incorrect value of state '"
                            + state + "' in the request");
                }
            });
        }

        return JpaAdminGetAllQueryParamsHolder.builder()
                .users(users)
                .states(states)
                .categories(categories)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .from(httpParams.getFrom())
                .size(httpParams.getSize())
                .build();
    }

    public static EventEntity getValidEventEntityToSaveRequest(EventEntity event) {
        int participantLimit = event.getParticipantLimit();
        int confirmedRequests = event.getConfirmedRequests();

        if (!EventState.PUBLISHED.name().equals(event.getState())) {
            throw new BadRequestBodyException("The event with id'" + event.getId() + "' is not published");
        }
        if (participantLimit == 0) {
            event.setConfirmedRequests(confirmedRequests + 1);
            return event;
        } else if (confirmedRequests < participantLimit) {
            if (!event.isRequestModeration()) {
                event.setConfirmedRequests(confirmedRequests + 1);
            }
            return event;
        } else {
            throw new BadRequestBodyException("Event participant limit is reached");
        }
    }

    public static void checkStatusCommandForSpecificLogic(RequestStatusSetRestCommand command) {
        Arrays.stream(command.getRequestIds()).forEach(id -> {
            if (id.compareTo(BigInteger.ZERO) <= 0) {
                throw new BadRequestParameterException(String.format("There is negative or zero id'%d' " +
                                "of request in the request", id));
            }
        });
        String status = command.getStatus();
        RequestStatus requestStatus;
        try {
            requestStatus = RequestStatus.valueOf(status);
        } catch (IllegalArgumentException exception) {
            throw new BadRequestParameterException(String.format("Failed to change status of requests: expected " +
                    "one of %s, but was '%s'", Arrays.toString(RequestStatus.values()), status));
        }
        if (!RequestStatus.CONFIRMED.equals(requestStatus) && !RequestStatus.REJECTED.equals(requestStatus)) {
            throw new BadRequestParameterException("Failed to change status of requests: status '" + requestStatus +
                    "' not supported for this operation.");
        }
    }

    public static void checkCompilationRestCommandForSpecificLogic(CompilationRestCommand compilationRestCommand) {
        checkStringField(compilationRestCommand.getTitle(), "title", 1, 50);
        Set<Long> eventsIds = compilationRestCommand.getEventsIds();
        if (eventsIds != null) {
            compilationRestCommand.getEventsIds().forEach(id -> {
                if (id <= 0) {
                    throw new BadRequestParameterException(String.format("There is negative or zero id'%d' " +
                            "of event in the request for saving/updating the compilation", id));
                }
            });
        }
    }

    private void checkDateTimeFromStringForSpecificLogic(String stringEventDate, boolean afterModeration) {
        if (stringEventDate == null) {
            return;
        }
        LocalDateTime eventDate;
        try {
            eventDate = LocalDateTime.parse(stringEventDate, EwmConstants.FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new BadRequestBodyException("Failed to create/update event: event date and time is in " +
                    "an unsupported format.");
        }
        if (eventDate.isBefore(LocalDateTime.now())) {       // Странная проверка eventDate, указанная в спецификации.
            // Наверное, было бы достаточно только проверки на минимальные 2 часа, которая идет после нее.
            throw new BadRequestParameterException("Failed to create/update event: event date and time cannot be earlier " +
                    "than current moment, but was '" + eventDate + "'");
        }
        if (afterModeration) {
            if (eventDate.isBefore(LocalDateTime.now().plusHours(1))) {
                throw new BadRequestBodyException("Failed to create/update event: event date and time cannot be earlier " +
                        "than one hour from the current moment, but was '" + eventDate + "'");
            }
        } else {
            if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new BadRequestBodyException("Failed to create/update event: event date and time cannot be earlier " +
                        "than two hours from the current moment, but was '" + eventDate + "'");
            }
        }
    }

    private LocalDateTime parseDateTimeFromHttpParams(String dateTime) {
        if (dateTime != null) {
            try {
                return LocalDateTime.parse(dateTime, EwmConstants.FORMATTER);
            } catch (DateTimeParseException exception) {
                throw new BadRequestParameterException("Wrong method parameter: range_start or range_end " +
                        "is in an unsupported format.");
            }
        }
        return null;
    }

    private void compareRangeStartAndEnd(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null) {
            if (rangeStart.isAfter(rangeEnd)) {
                throw new BadRequestParameterException("Wrong method parameter: " +
                        "start of period for requesting cannot be after its end. Start value '" + rangeStart + "'");
            }
            if (rangeStart.equals(rangeEnd)) {
                throw new BadRequestParameterException("Wrong method parameter: " +
                        "start of period for requesting cannot be equal to its end");
            }
        }
    }

    private void checkStringField(String value, String fieldName, int min, int max) {
        if (value != null) {
            if (value.isBlank()) {
                throw new BadRequestParameterException("Value of field '" + fieldName + "' must not be blank");
            }
            if (value.length() < min) {
                throw new BadRequestParameterException(String.format("The length of text value in field '%s' must be " +
                        "not less than '%d' characters", fieldName, min));
            }
            if (value.length() > max) {
                throw new BadRequestParameterException(String.format("The length of text value in field '%s' must be " +
                        "not greater than '%d' characters", fieldName, max));
            }
        }
    }

    private void checkArrayOfLongsForNegativeValues(long[] ids, String entity) {
        Arrays.stream(ids).forEach(id -> {
            if (id <= 0) {
                throw new BadRequestParameterException(String.format("There is negative or zero id'%d' " +
                                "of %s in the request", id, entity));
            }
        });
    }

}