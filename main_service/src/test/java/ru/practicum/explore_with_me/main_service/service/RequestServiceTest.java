package ru.practicum.explore_with_me.main_service.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.request.RequestStatus;
import ru.practicum.explore_with_me.main_service.model.rest_dto.category.CategoryRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.category.CategoryRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.EventRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.EventRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.GeoLocation;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.StateAction;
import ru.practicum.explore_with_me.main_service.model.rest_dto.request.*;
import ru.practicum.explore_with_me.main_service.model.rest_dto.user.UserRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.user.UserRestView;
import ru.practicum.explore_with_me.stats_service.client_submodule.StatsClient;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.EwmConstants;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.UriStatRestView;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
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

    private static final LocalDateTime DEFAULT_EVENT_DATE = EwmConstants.DEFAULT_DATE_TIME.plusMonths(2);
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
                .annotation("annotation_of_first_request")
                .description("description_of_first_request")
                .eventDate(DEFAULT_EVENT_DATE.format(EwmConstants.FORMATTER))
                .category(category.getId())
                .location(DEFAULT_LOCATION)
                .paid(true)
                .requestModeration(false)
                .participantLimit(10)
                .build());
        secondEvent = eventService.saveNewEvent(secondUser.getId(), EventRestCommand.builder()
                .title("title_2")
                .annotation("annotation_of_second_request")
                .description("description_of_second_request")
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
    public void setStatusToRequestsByInitiator_whenGetCorrectParameters_thenReturnModeratedRequestsRestView() {
        ModeratedRequestsRestView result = requestService.setStatusToRequestsByInitiator(
                firstUser.getId(), firstEvent.getId(), RequestStatusSetRestCommand.builder()
                        .requestIds(new BigInteger[] {secondRequest.getId(), BigInteger.valueOf(987654321L)})
                        .status(RequestStatus.CONFIRMED.name())
                        .build());

        assertThat(result, notNullValue());
        assertThat(result.getRejected(), emptyIterable());
        assertThat(result.getConfirmed(), emptyIterable());
        firstEvent = eventService.getEventById(firstEvent.getId());
        assertThat(firstEvent.getConfirmedRequests(), equalTo(1));
    }

}