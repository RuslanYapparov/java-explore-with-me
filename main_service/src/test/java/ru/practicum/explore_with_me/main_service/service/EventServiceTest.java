package ru.practicum.explore_with_me.main_service.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore_with_me.main_service.exception.ObjectModificationException;
import ru.practicum.explore_with_me.main_service.exception.BadRequestParameterException;
import ru.practicum.explore_with_me.main_service.exception.ObjectNotFoundException;
import ru.practicum.explore_with_me.main_service.exception.StatsServiceProblemException;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.event.EventState;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.params_holder.SortBy;
import ru.practicum.explore_with_me.main_service.model.rest_dto.category.CategoryRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.category.CategoryRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.*;
import ru.practicum.explore_with_me.main_service.model.rest_dto.user.UserRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.user.UserRestView;
import ru.practicum.explore_with_me.stats_service.client_submodule.StatsClient;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.EwmConstants;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.UriStatRestView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class EventServiceTest {
    private final EventService eventService;
    private final UserService userService;
    private final CategoryService categoryService;
    @MockBean
    StatsClient statsClient;

    private static final LocalDateTime DEFAULT_EVENT_DATE = EwmConstants.DEFAULT_DATE_TIME.plusMonths(2);
    private static final GeoLocation DEFAULT_LOCATION = GeoLocation.builder()
            .latitude(7.7D)
            .longitude(7.7D)
            .build();
    private EventRestView firstEvent;
    private EventRestView secondEvent;
    private UserRestView firstUser;
    private UserRestView secondUser;
    private CategoryRestView category;

    @BeforeEach
    public void prepareDbForTest_saveNewEvent_whenGetCorrectEventRestCommand_thenReturnEventRestView() {
        firstUser = userService.saveNewUser(UserRestCommand.builder()
                .name("user_1")
                .email("user_1@email.com")
                .build());
        secondUser = userService.saveNewUser(UserRestCommand.builder()
                .name("user_2")
                .email("user_2@email.com")
                .build());
        CategoryRestCommand categoryRestCommand = new CategoryRestCommand();
        categoryRestCommand.setName("category");
        category = categoryService.saveNewCategory(categoryRestCommand);

        assertThat(firstUser, notNullValue());
        assertTrue(firstUser.getId() >= 1L);
        assertThat(secondUser, notNullValue());
        assertTrue(secondUser.getId() >= 2L);
        assertThat(category, notNullValue());
        assertTrue(category.getId() >= 1L);

        firstEvent = eventService.saveNewEvent(firstUser.getId(), EventRestCommand.builder()
                .title("title_1")
                .annotation("annotation_of_first_event")
                .description("description_of_first_event")
                .eventDate(DEFAULT_EVENT_DATE.format(EwmConstants.FORMATTER))
                .category(category.getId())
                .location(DEFAULT_LOCATION)
                .paid(true)
                .requestModeration(false)
                .participantLimit(10)
                .build());
        secondEvent = eventService.saveNewEvent(secondUser.getId(), EventRestCommand.builder()
                .title("title_2")
                .annotation("annotation_of_second_event")
                .description("description_of_second_event")
                .eventDate(DEFAULT_EVENT_DATE.format(EwmConstants.FORMATTER))
                .category(category.getId())
                .location(DEFAULT_LOCATION)
                .build());

        assertThat(firstEvent, notNullValue());
        assertThat(secondEvent, notNullValue());
        assertTrue(firstEvent.getId() >= 1);
        assertTrue(secondEvent.getId() >= 2);

        when(statsClient.getUriStats(Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class),
                Mockito.any(String[].class), Mockito.anyBoolean()))
                .thenReturn(ResponseEntity.ok(new UriStatRestView[] {}));
    }

    @Test
    public void saveNewEvent_whenGetEventRestCommandWithNegativeParticipantLimit_thenThrowException() {
        assertThrows(ObjectModificationException.class, () ->
                eventService.saveNewEvent(firstUser.getId(), EventRestCommand.builder()
                        .title("title_2")
                        .annotation("annotation_of_second_event")
                        .description("description_of_second_event")
                        .eventDate(DEFAULT_EVENT_DATE.format(EwmConstants.FORMATTER))
                        .category(category.getId())
                        .location(DEFAULT_LOCATION)
                        .participantLimit(-1)
                        .build()));
    }

    @Test
    public void getAllEventsByParametersForAdmin_whenGetCorrectParameters_thenReturnListOfEventRestViews() {
        List<EventRestView> events = eventService.getAllEventsByParametersForAdmin(HttpAdminGetAllRequestParamsHolder
                .builder()
                .from(0)
                .size(10)
                .build());
        assertThat(events, notNullValue());
        assertThat(events, iterableWithSize(2));
        assertThat(events.get(0), equalTo(firstEvent));
        EventRestView secondEventFromList = events.get(1);
        assertThat(secondEventFromList, equalTo(secondEvent));
        assertThat(secondEventFromList.getTitle(), equalTo("title_2"));
        assertThat(secondEventFromList.getAnnotation(), equalTo("annotation_of_second_event"));
        assertThat(secondEventFromList.getDescription(), equalTo("description_of_second_event"));
        assertThat(secondEventFromList.getCategory().getId(), equalTo(category.getId()));
        assertThat(secondEventFromList.getInitiator().getId(), equalTo(secondUser.getId()));
        assertThat(secondEventFromList.getLocation(), equalTo(DEFAULT_LOCATION));
        assertTrue(secondEventFromList.getCreatedOn().isBefore(LocalDateTime.now()));
        assertThat(secondEventFromList.getEventDate(), equalTo(DEFAULT_EVENT_DATE));
        assertThat(secondEventFromList.getState(), equalTo(EventState.PENDING.name()));
        assertThat(secondEventFromList.getParticipantLimit(), equalTo(0));
        assertThat(secondEventFromList.getConfirmedRequests(), equalTo(0));
        assertThat(secondEventFromList.isPaid(), equalTo(false));
        assertThat(secondEventFromList.isRequestModeration(), equalTo(true));
        assertThat(secondEventFromList.getViews(), equalTo(0L));
        assertThat(secondEventFromList.getPublishedOn(), nullValue());

        verify(statsClient, Mockito.times(1))
                .getUriStats(Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class),
                        Mockito.any(String[].class), Mockito.anyBoolean());
    }

    @Test
    public void getAllEventsByParametersForAdmin_whenEventInDifferentStates_thenReturnAllEvents() {
        eventService.saveNewEvent(firstUser.getId(), EventRestCommand.builder()
                .title("title_3")
                .annotation("annotation_of_third_event")
                .description("description_of_third_event")
                .eventDate(DEFAULT_EVENT_DATE.format(EwmConstants.FORMATTER))
                .category(category.getId())
                .location(DEFAULT_LOCATION)
                .build());
        secondEvent = eventService.updateEventFromAdmin(secondEvent.getId(), EventRestCommand.builder()
                .stateAction(StateAction.PUBLISH_EVENT.name())
                .build());
        eventService.updateEventFromAdmin(secondEvent.getId() + 1, EventRestCommand.builder()
                .stateAction(StateAction.REJECT_EVENT.name())
                .build());

        List<EventRestView> events = eventService.getAllEventsByParametersForAdmin(HttpAdminGetAllRequestParamsHolder
                .builder()
                .from(0)
                .size(10)
                .build());
        assertThat(events, iterableWithSize(3));
        assertThat(events.get(0), equalTo(firstEvent));
        assertThat(events.get(1), equalTo(secondEvent));
        assertThat(events.get(2), equalTo(eventService.getAllEventsByParametersForAdmin(HttpAdminGetAllRequestParamsHolder
                .builder()
                .states(new String[] {"CANCELED"})
                .from(0)
                .size(10)
                .build()).get(0)));

        verify(statsClient, Mockito.times(4))
                .getUriStats(Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class),
                        Mockito.any(String[].class), Mockito.anyBoolean());
    }

    @Test
    public void getEventByIdAndGetAllEventsByParametersForAnyone_whenEventNotPublished_doNotReturnEvents() {
        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class, () ->
                eventService.getEventById(firstEvent.getId()));
        List<EventRestViewShort> events = eventService.getAllEventsByParametersForAnyone(
                HttpPublicGetAllRequestParamsHolder.builder()
                        .sort(SortBy.EVENT_DATE.name())
                        .from(0)
                        .size(10)
                        .build());

        assertThat(exception.getMessage(), equalTo("Requested event with id'" + firstEvent.getId()
                + "' not published"));
        assertThat(events, notNullValue());
        assertThat(events, emptyIterable());

        verify(statsClient, Mockito.never())
                .getUriStats(Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class),
                        Mockito.any(String[].class), Mockito.anyBoolean());
    }

    @Test
    public void getAllEventsByParametersForAdmin_whenGetAllFilterParameters_thenReturnAllEvents() {
        eventService.saveNewEvent(firstUser.getId(), EventRestCommand.builder()
                .title("title_3")
                .annotation("annotation_of_third_event")
                .description("description_of_third_event")
                .eventDate(DEFAULT_EVENT_DATE.format(EwmConstants.FORMATTER))
                .category(category.getId())
                .location(DEFAULT_LOCATION)
                .build());
        secondEvent = eventService.updateEventFromAdmin(secondEvent.getId(), EventRestCommand.builder()
                .stateAction(StateAction.PUBLISH_EVENT.name())
                .build());
        eventService.updateEventFromAdmin(secondEvent.getId() + 1, EventRestCommand.builder()
                .stateAction(StateAction.PUBLISH_EVENT.name())
                .build());

        List<EventRestView> events = eventService.getAllEventsByParametersForAdmin(HttpAdminGetAllRequestParamsHolder
                .builder()
                .users(new long[] {firstUser.getId(), secondUser.getId(), 987654321, 77777777})
                .categories(new long[] {category.getId(), 987654321, 777777777})
                .rangeStart(EwmConstants.DEFAULT_DATE_TIME.format(EwmConstants.FORMATTER))
                .rangeEnd(LocalDateTime.now().plusYears(1).format(EwmConstants.FORMATTER))
                .states(new String[] {"PUBLISHED", "PENDING"})
                .from(0)
                .size(10)
                .build());
        assertThat(events, iterableWithSize(3));
        assertThat(events.get(0), equalTo(firstEvent));
        assertThat(events.get(1), equalTo(secondEvent));
        assertThat(events.get(2), equalTo(eventService.getAllEventsByParametersForAdmin(HttpAdminGetAllRequestParamsHolder
                .builder()
                .states(new String[] {"PUBLISHED"})
                .from(0)
                .size(10)
                .build()).get(1)));

        verify(statsClient, Mockito.times(4))
                .getUriStats(Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class),
                        Mockito.any(String[].class), Mockito.anyBoolean());
    }

    @Test
    public void getAllEventsByParametersForAnyone_whenGetAllFilterParameters_thenReturnAllEvents() {
        eventService.saveNewEvent(firstUser.getId(), EventRestCommand.builder()
                .title("title_3")
                .annotation("annotation_of_third_event")
                .description("description_of_third_event")
                .eventDate(DEFAULT_EVENT_DATE.format(EwmConstants.FORMATTER))
                .category(category.getId())
                .location(DEFAULT_LOCATION)
                .build());
        secondEvent = eventService.updateEventFromAdmin(secondEvent.getId(), EventRestCommand.builder()
                .stateAction(StateAction.PUBLISH_EVENT.name())
                .build());
        eventService.updateEventFromAdmin(secondEvent.getId() + 1, EventRestCommand.builder()
                .stateAction(StateAction.PUBLISH_EVENT.name())
                .build());

        List<EventRestViewShort> events = eventService.getAllEventsByParametersForAnyone(HttpPublicGetAllRequestParamsHolder
                .builder()
                .text("event")
                .categories(new long[] {category.getId(), 987654321, 777777777})
                .onlyAvailable(false)
                .paid(false)
                .rangeStart(EwmConstants.DEFAULT_DATE_TIME.format(EwmConstants.FORMATTER))
                .rangeEnd(LocalDateTime.now().plusYears(1).format(EwmConstants.FORMATTER))
                .sort(SortBy.VIEWS.name())
                .from(0)
                .size(10)
                .build());
        assertThat(events, iterableWithSize(2));
        assertThat(events.get(0).getId(), equalTo(secondEvent.getId()));
        assertThat(events.get(1).getId(), equalTo(secondEvent.getId() + 1));

        verify(statsClient, Mockito.times(3))
                .getUriStats(Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class),
                        Mockito.any(String[].class), Mockito.anyBoolean());
    }

    @Test
    public void updateEventFromAdmin_whenGetPublishStateAction_thenReturnPublishedEvent() {
        firstEvent = eventService.updateEventFromAdmin(firstEvent.getId(), EventRestCommand.builder()
                .stateAction(StateAction.PUBLISH_EVENT.name())
                .build());
        assertThat(firstEvent.getState(), equalTo(EventState.PUBLISHED.name()));
        assertThat(firstEvent.getPublishedOn(), notNullValue());
        assertTrue(firstEvent.getPublishedOn().isAfter(LocalDateTime.now().minusSeconds(2)));

        EventRestView eventFromService = eventService.getEventById(firstEvent.getId());
        assertThat(eventFromService, equalTo(firstEvent));
        eventFromService = eventService.getEventOfUserById(firstUser.getId(), firstEvent.getId());
        assertThat(eventFromService, equalTo(firstEvent));

        verify(statsClient, Mockito.times(3))
                .getUriStats(Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class),
                        Mockito.any(String[].class), Mockito.anyBoolean());
    }

    @Test
    public void updateEventFromAdmin_whenGetRejectStateAction_thenReturnCancelledEvent() {
        eventService.updateEventFromAdmin(firstEvent.getId(), EventRestCommand.builder()
                .stateAction(StateAction.REJECT_EVENT.name())
                .build());

        assertThrows(ObjectNotFoundException.class, () ->
                eventService.getEventById(firstEvent.getId()));

        firstEvent = eventService.getEventOfUserById(firstUser.getId(), firstEvent.getId());
        assertThat(firstEvent.getState(), equalTo(EventState.CANCELED.name()));
        assertThat(firstEvent.getPublishedOn(), nullValue());

        verify(statsClient, Mockito.times(2))
                .getUriStats(Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class),
                        Mockito.any(String[].class), Mockito.anyBoolean());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\n", "\r", "\t"})
    public void updateEvent_whenGetEventRestCommandWithEmptyField_thenThrowException(String value) {
        assertThrows(BadRequestParameterException.class, () ->
                eventService.updateEventFromInitiator(firstUser.getId(), firstEvent.getId(), EventRestCommand.builder()
                        .title(value)
                        .build()));
        assertThrows(BadRequestParameterException.class, () ->
                eventService.updateEventFromInitiator(firstUser.getId(), firstEvent.getId(), EventRestCommand.builder()
                        .annotation(value)
                        .build()));
        assertThrows(BadRequestParameterException.class, () ->
                eventService.updateEventFromInitiator(firstUser.getId(), firstEvent.getId(), EventRestCommand.builder()
                        .description(value)
                        .build()));
        assertThrows(ObjectModificationException.class, () ->
                eventService.updateEventFromInitiator(firstUser.getId(), firstEvent.getId(), EventRestCommand.builder()
                        .eventDate(value)
                        .build()));
        assertThrows(ObjectModificationException.class, () ->
                eventService.updateEventFromInitiator(firstUser.getId(), firstEvent.getId(), EventRestCommand.builder()
                        .stateAction(value)
                        .build()));
    }

    @Test
    public void updateEvent_whenGetEventRestCommandWithIncorrectField_thenThrowException() {
        assertThrows(BadRequestParameterException.class, () ->
                eventService.updateEventFromInitiator(firstUser.getId(), firstEvent.getId(), EventRestCommand.builder()
                        .title("aa")
                        .build()));
        assertThrows(BadRequestParameterException.class, () ->
                eventService.updateEventFromAdmin(firstEvent.getId(), EventRestCommand.builder()
                        .title("a".repeat(121))
                        .build()));
        assertThrows(BadRequestParameterException.class, () ->
                eventService.updateEventFromAdmin(firstEvent.getId(), EventRestCommand.builder()
                        .annotation("a".repeat(19))
                        .build()));
        assertThrows(BadRequestParameterException.class, () ->
                eventService.updateEventFromInitiator(firstUser.getId(), firstEvent.getId(), EventRestCommand.builder()
                        .annotation("a".repeat(2001))
                        .build()));
        assertThrows(BadRequestParameterException.class, () ->
                eventService.updateEventFromAdmin(firstEvent.getId(), EventRestCommand.builder()
                        .description("a".repeat(19))
                        .build()));
        assertThrows(BadRequestParameterException.class, () ->
                eventService.updateEventFromInitiator(firstUser.getId(), firstEvent.getId(), EventRestCommand.builder()
                        .annotation("a".repeat(7001))
                        .build()));
        assertThrows(BadRequestParameterException.class, () ->
                eventService.updateEventFromAdmin(firstEvent.getId(), EventRestCommand.builder()
                        .eventDate(LocalDateTime.now().minusSeconds(1).format(EwmConstants.FORMATTER))
                        .build()));
        assertThrows(ObjectModificationException.class, () ->
                eventService.updateEventFromAdmin(firstEvent.getId(), EventRestCommand.builder()
                        .eventDate(LocalDateTime.now().plusMinutes(59).format(EwmConstants.FORMATTER))
                        .build()));
        assertThrows(ObjectModificationException.class, () ->
                eventService.updateEventFromInitiator(firstUser.getId(), firstEvent.getId(), EventRestCommand.builder()
                        .eventDate(LocalDateTime.now().plusMinutes(119).format(EwmConstants.FORMATTER))
                        .build()));
    }

    @Test
    public void getAllEventsByParametersForAdmin_whenGetIncorrectParameters_thenThrowException() {
        assertThrows(BadRequestParameterException.class, () ->
                eventService.getAllEventsByParametersForAdmin(HttpAdminGetAllRequestParamsHolder.builder()
                        .states(new String[] {"PENDING", "PUBLISHED", "DISTURBED"})
                        .from(0)
                        .size(10)
                        .build()));

        assertThrows(BadRequestParameterException.class, () ->
                eventService.getAllEventsByParametersForAdmin(HttpAdminGetAllRequestParamsHolder.builder()
                        .rangeStart(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                        .from(0)
                        .size(10)
                        .build()));

        assertThrows(BadRequestParameterException.class, () ->
                eventService.getAllEventsByParametersForAdmin(HttpAdminGetAllRequestParamsHolder.builder()
                        .rangeStart(LocalDateTime.now().plusMinutes(1).format(EwmConstants.FORMATTER))
                        .rangeEnd(LocalDateTime.now().format(EwmConstants.FORMATTER))
                        .from(0)
                        .size(10)
                        .build()));

        assertThrows(BadRequestParameterException.class, () ->
                eventService.getAllEventsByParametersForAdmin(HttpAdminGetAllRequestParamsHolder.builder()
                        .rangeStart(EwmConstants.DEFAULT_DATE_TIME.format(EwmConstants.FORMATTER))
                        .rangeEnd(EwmConstants.DEFAULT_DATE_TIME.format(EwmConstants.FORMATTER))
                        .from(0)
                        .size(10)
                        .build()));

        verify(statsClient, Mockito.never())
                .getUriStats(Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class),
                        Mockito.any(String[].class), Mockito.anyBoolean());
    }

    @Test
    public void getAllEventsByParametersForAnyone_whenGetIncorrectParameters_thenThrowException() {
        assertThrows(BadRequestParameterException.class, () ->
                eventService.getAllEventsByParametersForAnyone(HttpPublicGetAllRequestParamsHolder.builder()
                        .categories(new long[] {777, category.getId(), -1})
                        .sort(SortBy.EVENT_DATE.name())
                        .from(0)
                        .size(10)
                        .build()));

        assertThrows(BadRequestParameterException.class, () ->
                eventService.getAllEventsByParametersForAnyone(HttpPublicGetAllRequestParamsHolder.builder()
                        .sort("IDENTIFIERS")
                        .from(0)
                        .size(10)
                        .build()));

        verify(statsClient, Mockito.never())
                .getUriStats(Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class),
                        Mockito.any(String[].class), Mockito.anyBoolean());
    }

    @Test
    public void getAllEventsByUserId_whenGetCorrectParameters_thenReturnListOfEvents() {
        eventService.saveNewEvent(firstUser.getId(), EventRestCommand.builder()
                .title("title_3")
                .annotation("annotation_of_third_event")
                .description("description_of_third_event")
                .eventDate(DEFAULT_EVENT_DATE.format(EwmConstants.FORMATTER))
                .category(category.getId())
                .location(DEFAULT_LOCATION)
                .build());
        eventService.updateEventFromAdmin(firstEvent.getId(), EventRestCommand.builder()
                .stateAction(StateAction.REJECT_EVENT.name())
                .build());
        eventService.updateEventFromAdmin(secondEvent.getId() + 1, EventRestCommand.builder()
                .stateAction(StateAction.PUBLISH_EVENT.name())
                .build());

        List<EventRestViewShort> events = eventService.getAllEventsByUserId(firstUser.getId(), 0, 10);

        assertThat(events, notNullValue());
        assertThat(events, iterableWithSize(2));
        assertThat(events.get(0).getId(), equalTo(firstEvent.getId()));
        assertThat(events.get(1).getId(), equalTo(secondEvent.getId() + 1));

        verify(statsClient, Mockito.times(3))
                .getUriStats(Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class),
                        Mockito.any(String[].class), Mockito.anyBoolean());
    }

    @Test
    public void updateEventFromInitiator_whenUserCancelReview_thenReturnCanceledEventRestView() {
        eventService.updateEventFromInitiator(firstUser.getId(), firstEvent.getId(), EventRestCommand.builder()
                .stateAction(StateAction.CANCEL_REVIEW.name())
                .build());

        firstEvent = eventService.getEventOfUserById(firstUser.getId(), firstEvent.getId());

        assertThat(firstEvent.getState(), equalTo(EventState.CANCELED.name()));

        eventService.updateEventFromInitiator(firstUser.getId(), firstEvent.getId(), EventRestCommand.builder()
                .stateAction(StateAction.SEND_TO_REVIEW.name())
                .build());

        firstEvent = eventService.getEventOfUserById(firstUser.getId(), firstEvent.getId());

        assertThat(firstEvent.getState(), equalTo(EventState.PENDING.name()));

        verify(statsClient, Mockito.times(4))
                .getUriStats(Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class),
                        Mockito.any(String[].class), Mockito.anyBoolean());
    }

    @Test
    public void mapEventEntitiesToEventsWithViews_whenStatsServiceReturnNotCorrectResponse_thenThrowException() {
        when(statsClient.getUriStats(Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class),
                Mockito.any(String[].class), Mockito.anyBoolean()))
                .thenReturn(ResponseEntity.ok(new UriStatRestView[] {UriStatRestView.builder()
                        .application("application")
                        .uri("/events/requests/57")
                        .hits(7L)
                        .build()}));

        StatsServiceProblemException exception = assertThrows(StatsServiceProblemException.class, () ->
                eventService.getAllEventsByUserId(firstUser.getId(), 0, 10));
        assertThat(exception.getMessage(), equalTo("Unsupported URI format found in response from " +
                "Stats_service: /events/requests/57"));
    }

    @Test
    public void saveNewEvent_whenGetDoubleInitiatorDateTitleTest_thenThrowException() {
        DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class, () ->
                eventService.saveNewEvent(firstUser.getId(), EventRestCommand.builder()
                        .title("title_1")
                        .annotation("annotation_of_first_event")
                        .description("description_of_first_event")
                        .eventDate(DEFAULT_EVENT_DATE.format(EwmConstants.FORMATTER))
                        .category(category.getId())
                        .location(DEFAULT_LOCATION)
                        .paid(true)
                        .requestModeration(false)
                        .participantLimit(10)
                        .build()));
        assertThat(exception.getMessage(), equalTo("could not execute statement; SQL [n/a]; " +
                "constraint [null]; nested exception is org.hibernate.exception.ConstraintViolationException: " +
                "could not execute statement"));
    }

}