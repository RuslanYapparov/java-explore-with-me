package ru.practicum.explore_with_me.main_service.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
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
import ru.practicum.explore_with_me.main_service.model.domain_pojo.request.RequestStatus;
import ru.practicum.explore_with_me.main_service.model.rest_dto.category.CategoryRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.category.CategoryRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.*;
import ru.practicum.explore_with_me.main_service.model.rest_dto.request.*;
import ru.practicum.explore_with_me.main_service.model.rest_dto.user.UserRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.user.UserRestView;
import ru.practicum.explore_with_me.stats_service.client_submodule.StatsClient;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.EwmConstants;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.UriStatRestView;

import javax.validation.ConstraintViolationException;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RequestServiceTest {
    private final RequestService requestService;
    private final UserService userService;
    private final EventService eventService;
    private final CategoryService categoryService;
    @MockBean
    StatsClient statsClient;

    private static final LocalDateTime DEFAULT_EVENT_DATE = EwmConstants.DEFAULT_DATE_TIME.plusYears(2);
    private static final GeoLocation DEFAULT_LOCATION = GeoLocation.builder()
            .latitude(7.7D)
            .longitude(7.7D)
            .build();
    private UserRestView firstUser;
    private UserRestView secondUser;
    private EventRestView firstEvent;
    private EventRestView secondEvent;
    private RequestRestView firstRequest;
    private RequestRestView secondRequest;
    private CategoryRestView category;

    @BeforeEach
    public void prepareDbForTest_saveNewRequest_whenGetCorrectRequestRestCommand_thenReturnRequestRestView() {
        when(statsClient.getUriStats(Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class),
                Mockito.any(String[].class), Mockito.anyBoolean()))
                .thenReturn(ResponseEntity.ok(new UriStatRestView[] {}));

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

        eventService.updateEventFromAdmin(firstEvent.getId(), EventRestCommand.builder()
                .stateAction(StateAction.PUBLISH_EVENT.name())
                .build());
        eventService.updateEventFromAdmin(secondEvent.getId(), EventRestCommand.builder()
                .stateAction(StateAction.PUBLISH_EVENT.name())
                .build());
        firstRequest = requestService.saveNewRequest(RequestRestCommand.builder()
                .requester(firstUser.getId())
                .event(secondEvent.getId())
                .build());
        secondRequest = requestService.saveNewRequest(RequestRestCommand.builder()
                .requester(secondUser.getId())
                .event(firstEvent.getId())
                .build());

        assertThat(firstRequest, notNullValue());
        assertThat(secondRequest, notNullValue());
        assertThat(firstRequest.getId(), greaterThan(BigInteger.ZERO));
        assertThat(secondRequest.getId(), greaterThan(BigInteger.ONE));
    }

    @Test
    public void getAllRequestsOfUser_whenGetCorrectParameters_thenReturnListOfRequestRestViews() {
        List<RequestRestView> requests = requestService.getAllRequestsOfUser(firstUser.getId());
        assertThat(requests, notNullValue());
        assertThat(requests, iterableWithSize(1));
        RequestRestView requestFromList = requests.get(0);
        assertThat(requestFromList.getRequester(), equalTo(firstUser.getId()));
        assertThat(requestFromList.getEvent(), equalTo(secondEvent.getId()));
        assertThat(requestFromList.getStatus(), equalTo(RequestStatus.CONFIRMED.name()));
        assertTrue(requestFromList.getCreatedOn().isAfter(LocalDateTime.now().minusSeconds(2)));

        verify(statsClient, Mockito.times(2))
                .getUriStats(Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class),
                        Mockito.any(String[].class), Mockito.anyBoolean());
    }

    @Test
    public void getAllRequestsToEventForInitiator_whenGetCorrectParameters_thenReturnListOfRequestRestViews() {
        List<RequestRestView> requests = requestService.getAllRequestsToEventForInitiator(
                firstUser.getId(), firstEvent.getId());
        assertThat(requests, notNullValue());
        assertThat(requests, iterableWithSize(1));
        assertThat(requests.get(0), equalTo(secondRequest));

        requests = requestService.getAllRequestsToEventForInitiator(secondUser.getId(), secondEvent.getId());
        assertThat(requests, notNullValue());
        assertThat(requests, iterableWithSize(1));
        assertThat(requests.get(0), equalTo(firstRequest));

        verify(statsClient, Mockito.times(2))
                .getUriStats(Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class),
                        Mockito.any(String[].class), Mockito.anyBoolean());
    }

    @Test
    public void setStatusToRequestsByInitiator_whenGetCorrectParameters_thenReturnModeratedRequestsRestView() {
        ModeratedRequestsRestView result = requestService.setStatusToRequestsByInitiator(
                firstUser.getId(), firstEvent.getId(), RequestStatusSetRestCommand.builder()
                        .requestIds(new BigInteger[] {secondRequest.getId(), BigInteger.valueOf(987654321L)})
                        .status(RequestStatus.CONFIRMED.name())
                        .build());

        assertThat(result, notNullValue());
        assertThat(result.getRejected(), emptyIterable());
        assertThat(result.getConfirmed(), iterableWithSize(1));
        firstEvent = eventService.getEventById(firstEvent.getId());
        assertThat(firstEvent.getConfirmedRequests(), equalTo(1));

        result = requestService.setStatusToRequestsByInitiator(
                secondUser.getId(), secondEvent.getId(), RequestStatusSetRestCommand.builder()
                        .requestIds(new BigInteger[] {firstRequest.getId(), BigInteger.valueOf(987654321L)})
                        .status(RequestStatus.REJECTED.name())
                        .build());
        assertThat(result, notNullValue());
        assertThat(result.getRejected(), emptyIterable());
        assertThat(result.getConfirmed(), iterableWithSize(1));
        firstRequest = requestService.getAllRequestsOfUser(firstUser.getId()).get(0);
        assertThat(firstRequest.getStatus(), equalTo(RequestStatus.CONFIRMED.name()));
        secondEvent = eventService.getEventById(secondEvent.getId());
        assertThat(secondEvent.getConfirmedRequests(), equalTo(1));

        EventRestView thirdEvent = eventService.saveNewEvent(firstUser.getId(), EventRestCommand.builder()
                .title("title_3")
                .annotation("annotation_of_third_request")
                .description("description_of_third_request")
                .eventDate(DEFAULT_EVENT_DATE.format(EwmConstants.FORMATTER))
                .category(category.getId())
                .location(DEFAULT_LOCATION)
                .paid(true)
                .requestModeration(true)
                .participantLimit(10)
                .build());
        eventService.updateEventFromAdmin(thirdEvent.getId(), EventRestCommand.builder()
                .stateAction(StateAction.PUBLISH_EVENT.name())
                .build());
        RequestRestView requestRestView = requestService.saveNewRequest(RequestRestCommand.builder()
                .requester(secondUser.getId())
                .event(thirdEvent.getId())
                .build());

        thirdEvent = eventService.getEventById(thirdEvent.getId());
        assertThat(thirdEvent.getConfirmedRequests(), equalTo(0));
        assertThat(requestRestView.getStatus(), equalTo(RequestStatus.PENDING.name()));

        result = requestService.setStatusToRequestsByInitiator(firstUser.getId(), thirdEvent.getId(),
                RequestStatusSetRestCommand.builder()
                        .requestIds(new BigInteger[] { requestRestView.getId() })
                        .status(RequestStatus.CONFIRMED.name())
                        .build());
        assertThat(result.getConfirmed(), iterableWithSize(1));
        assertThat(result.getRejected(), iterableWithSize(0));
        thirdEvent = eventService.getEventById(thirdEvent.getId());
        assertThat(thirdEvent.getConfirmedRequests(), equalTo(1));
        requestRestView = requestService.getAllRequestsOfUser(secondUser.getId()).get(1);
        assertThat(RequestStatus.CONFIRMED.name(), equalTo(requestRestView.getStatus()));
    }

    @Test
    public void cancelRequestByRequester_whenGetCorrectParameters_thenReturnCancelledRequestRestView() {
        secondRequest = requestService.cancelRequestByRequester(secondUser.getId(), secondRequest.getId());

        assertThat(secondRequest.getStatus(), equalTo(RequestStatus.CANCELED.name()));
        firstEvent = eventService.getEventById(firstEvent.getId());
        assertThat(firstEvent.getConfirmedRequests(), equalTo(0));
    }

    @Test
    public void saveNewRequest_whenDoubleRequestOrRequestOwnEvent_thenThrowException() {
        assertThrows(DataIntegrityViolationException.class, () ->
                requestService.saveNewRequest(RequestRestCommand.builder()
                        .requester(firstUser.getId())
                        .event(secondEvent.getId())
                        .build()));

        ObjectModificationException exception = assertThrows(ObjectModificationException.class, () ->
                requestService.saveNewRequest(RequestRestCommand.builder()
                        .requester(firstUser.getId())
                        .event(firstEvent.getId())
                        .build()));
        assertThat(exception.getMessage(), equalTo("Failed to save request: initiator can't request participation " +
                "in his event"));
    }

    @Test
    public void getAllRequestsToEventForInitiator_whenGetNotInitiatorId_thenThrowException() {
        assertThrows(BadRequestParameterException.class, () ->
                requestService.getAllRequestsToEventForInitiator(secondUser.getId(), firstEvent.getId()));
    }

    @Test
    public void setStatusToRequestsByInitiator_whenGetNotInitiatorId_thenThrowException() {
        assertThrows(BadRequestParameterException.class, () ->
                requestService.setStatusToRequestsByInitiator(firstUser.getId(), secondEvent.getId(),
                        RequestStatusSetRestCommand.builder()
                                .requestIds(new BigInteger[] {secondRequest.getId(), BigInteger.valueOf(987654321L)})
                                .status(RequestStatus.CONFIRMED.name())
                                .build()));
    }

    @Test
    public void cancelRequestByRequester_whenGetNotRequesterId_thenThrowException() {
        assertThrows(BadRequestParameterException.class, () ->
                requestService.cancelRequestByRequester(firstUser.getId(), secondRequest.getId()));
    }

    @Test
    public void cancelRequestByRequester_whenGCancelRejectedRequest_thenThrowException() {
        EventRestView thirdEvent = eventService.saveNewEvent(firstUser.getId(), EventRestCommand.builder()
                .title("title_3")
                .annotation("annotation_of_third_request")
                .description("description_of_third_request")
                .eventDate(DEFAULT_EVENT_DATE.format(EwmConstants.FORMATTER))
                .category(category.getId())
                .location(DEFAULT_LOCATION)
                .paid(true)
                .requestModeration(true)
                .participantLimit(10)
                .build());
        eventService.updateEventFromAdmin(thirdEvent.getId(), EventRestCommand.builder()
                .stateAction(StateAction.PUBLISH_EVENT.name())
                .build());
        RequestRestView requestRestView = requestService.saveNewRequest(RequestRestCommand.builder()
                .requester(secondUser.getId())
                .event(thirdEvent.getId())
                .build());
        requestService.setStatusToRequestsByInitiator(firstUser.getId(), thirdEvent.getId(),
                RequestStatusSetRestCommand.builder()
                        .requestIds(new BigInteger[] { requestRestView.getId() })
                        .status(RequestStatus.REJECTED.name())
                        .build());

        assertThrows(ObjectModificationException.class, () ->
                requestService.cancelRequestByRequester(secondUser.getId(), requestRestView.getId()));
    }

    @ParameterizedTest
    @ValueSource(strings = { "CANCELLED", "PENDING", "CORRECTED" })
    public void setStatusToRequestsByInitiator_whenGetIncorrectStatus_thenThrowException(String value) {
        assertThrows(BadRequestParameterException.class, () ->
                requestService.setStatusToRequestsByInitiator(firstUser.getId(), firstEvent.getId(),
                        RequestStatusSetRestCommand.builder()
                                .requestIds(new BigInteger[] { secondRequest.getId() })
                                .status(value)
                                .build()));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " ", "       ", "\r", "\t", "\n" })
    @NullSource
    public void setStatusToRequestsByInitiator_whenGetEmptyOrNullStatus_thenThrowException(String value) {
        assertThrows(ConstraintViolationException.class, () ->
                requestService.setStatusToRequestsByInitiator(firstUser.getId(), firstEvent.getId(),
                        RequestStatusSetRestCommand.builder()
                                .requestIds(new BigInteger[] { secondRequest.getId() })
                                .status(value)
                                .build()));
    }

    @ParameterizedTest
    @ValueSource(longs = { 0, -1 })
    public void allMethods_whenGetIncorrectIdParameters_thenThrowException(long value) {
        assertThrows(ConstraintViolationException.class, () ->
                requestService.saveNewRequest(RequestRestCommand.builder()
                        .requester(value)
                        .event(secondEvent.getId())
                        .build()));

        assertThrows(ConstraintViolationException.class, () ->
                requestService.saveNewRequest(RequestRestCommand.builder()
                        .requester(firstEvent.getId())
                        .event(value)
                        .build()));

        assertThrows(ConstraintViolationException.class, () ->
                requestService.getAllRequestsOfUser(value));

        assertThrows(ConstraintViolationException.class, () ->
                requestService.cancelRequestByRequester(value, BigInteger.ONE));

        assertThrows(ConstraintViolationException.class, () ->
                requestService.cancelRequestByRequester(value, BigInteger.valueOf(value)));

        assertThrows(ConstraintViolationException.class, () ->
                requestService.getAllRequestsToEventForInitiator(value, firstEvent.getId()));

        assertThrows(ConstraintViolationException.class, () ->
                requestService.getAllRequestsToEventForInitiator(firstUser.getId(), value));

        assertThrows(ConstraintViolationException.class, () ->
                requestService.setStatusToRequestsByInitiator(value, firstEvent.getId(), RequestStatusSetRestCommand.builder()
                        .status("CONFIRMED")
                        .requestIds(new BigInteger[] {BigInteger.ONE})
                        .build()));

        assertThrows(ConstraintViolationException.class, () ->
                requestService.setStatusToRequestsByInitiator(firstUser.getId(), value, RequestStatusSetRestCommand.builder()
                        .status("CONFIRMED")
                        .requestIds(new BigInteger[] {BigInteger.ONE})
                        .build()));

        assertThrows(BadRequestParameterException.class, () ->
                requestService.setStatusToRequestsByInitiator(firstUser.getId(), firstEvent.getId(),
                        RequestStatusSetRestCommand.builder()
                        .status("CONFIRMED")
                        .requestIds(new BigInteger[] {BigInteger.valueOf(value)})
                        .build()));
    }

    @Test
    public void allMethods_whenGetNotExistingId_thenThrowNotExistingException() {
        long notExistingUserId = secondUser.getId() + 1;
        long notExistingEventId = secondEvent.getId() + 2;
        BigInteger notExistingRequestId = secondRequest.getId().add(BigInteger.ONE);
        EventRestView thirdEvent = eventService.saveNewEvent(firstUser.getId(), EventRestCommand.builder()
                .title("title_3")
                .annotation("annotation_of_third_request")
                .description("description_of_third_request")
                .eventDate(DEFAULT_EVENT_DATE.format(EwmConstants.FORMATTER))
                .category(category.getId())
                .location(DEFAULT_LOCATION)
                .paid(true)
                .requestModeration(true)
                .participantLimit(10)
                .build());
        eventService.updateEventFromAdmin(thirdEvent.getId(), EventRestCommand.builder()
                .stateAction(StateAction.PUBLISH_EVENT.name())
                .build());

        assertThrows(ObjectNotFoundException.class, () ->
                requestService.saveNewRequest(RequestRestCommand.builder()
                        .requester(notExistingUserId)
                        .event(thirdEvent.getId())
                        .build()));
        assertThrows(ObjectNotFoundException.class, () ->
                requestService.saveNewRequest(RequestRestCommand.builder()
                        .requester(firstUser.getId())
                        .event(notExistingEventId)
                        .build()));
        assertThrows(ObjectNotFoundException.class, () ->
                requestService.getAllRequestsOfUser(notExistingUserId));
        assertThrows(ObjectNotFoundException.class, () ->
                requestService.getAllRequestsToEventForInitiator(notExistingUserId, firstEvent.getId()));
        assertThrows(ObjectNotFoundException.class, () ->
                requestService.getAllRequestsToEventForInitiator(secondUser.getId(), notExistingEventId));
        assertThrows(ObjectNotFoundException.class, () ->
                requestService.cancelRequestByRequester(notExistingUserId, firstRequest.getId()));
        assertThrows(ObjectNotFoundException.class, () ->
                requestService.cancelRequestByRequester(firstUser.getId(), notExistingRequestId));
        assertThrows(ObjectNotFoundException.class, () ->
                requestService.setStatusToRequestsByInitiator(notExistingUserId, firstEvent.getId(),
                        RequestStatusSetRestCommand.builder()
                                .requestIds(new BigInteger[] { secondRequest.getId() })
                                .status("CONFIRMED")
                                .build()));
        assertThrows(ObjectNotFoundException.class, () ->
                requestService.setStatusToRequestsByInitiator(firstUser.getId(), notExistingEventId,
                        RequestStatusSetRestCommand.builder()
                                .requestIds(new BigInteger[] { secondRequest.getId() })
                                .status("CONFIRMED")
                                .build()));
    }

}