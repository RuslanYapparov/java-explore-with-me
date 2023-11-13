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

import ru.practicum.explore_with_me.main_service.exception.BadRequestParameterException;
import ru.practicum.explore_with_me.main_service.exception.ObjectNotFoundException;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.params_holder.SortBy;
import ru.practicum.explore_with_me.main_service.model.rest_dto.category.CategoryRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.category.CategoryRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.*;
import ru.practicum.explore_with_me.main_service.model.rest_dto.like.LikeRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.like.LikedEventsRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.like.WhoLikedRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.user.UserRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.user.UserRestView;
import ru.practicum.explore_with_me.stats_service.client_submodule.StatsClient;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.EwmConstants;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.UriStatRestView;

import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class LikeServiceTest {
    private final LikeService likeService;
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
    private final List<UserRestView> users = new ArrayList<>();
    private List<EventRestView> events = new ArrayList<>();
    private UserRestView firstUser;
    private UserRestView secondUser;
    private EventRestView firstEvent;
    private EventRestView secondEvent;
    private CategoryRestView categoryOne;

    @BeforeEach
    public void prepareStatsClient() {
        when(statsClient.getUriStats(Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class),
                Mockito.any(String[].class), Mockito.anyBoolean()))
                .thenReturn(ResponseEntity.ok(new UriStatRestView[] {}));
    }

    @Test
    public void saveNewLike_whenInitiatorClickedLikeToHisEvent_thenReturnEventWithLike() {
        prepareLittleDbForTest();
        firstEvent = likeService.saveNewLike(LikeRestCommand.builder()
                .user(firstUser.getId())
                .event(firstEvent.getId())
                .isLike(true)
                .build());

        assertThat(firstEvent.getNumberOfLikes(), equalTo(2));
        assertThat(firstEvent.getRating(), equalTo(0));
        List<UserRestView> sortedUsers = userService.getInitiatorsSortedByRating(0, 10, true);
        assertThat(sortedUsers, iterableWithSize(2));
        assertThat(sortedUsers.get(0).getId(), equalTo(firstUser.getId()));
        assertThat(sortedUsers.get(1).getId(), equalTo(secondUser.getId()));
    }

    @Test
    public void saveNewLike_whenGetDoubleLike_thenThrowException() {
        prepareLittleDbForTest();
        assertThrows(DataIntegrityViolationException.class, () ->
                likeService.saveNewLike(LikeRestCommand.builder()
                        .user(firstUser.getId())
                        .event(secondEvent.getId())
                        .isLike(false)
                        .build()));
    }

    @Test
    public void getAllEventsLikedByUser_whenUserClickedLikes_thenReturnLikedEventsRestView() {
        prepareLittleDbForTest();
        likeService.saveNewLike(LikeRestCommand.builder()
                .user(firstUser.getId())
                .event(firstEvent.getId())
                .isLike(false)
                .build());

        LikedEventsRestView likedEventsRestView = likeService.getAllEventsLikedByUser(firstUser.getId());
        assertThat(likedEventsRestView, notNullValue());
        assertThat(likedEventsRestView.getLiked(), iterableWithSize(1));
        assertThat(likedEventsRestView.getDisliked(), iterableWithSize(1));
        List<EventRestViewShort> listOfShorts = new ArrayList<>(likedEventsRestView.getLiked());
        assertThat(listOfShorts.get(0).getId(), equalTo(secondEvent.getId()));

        likedEventsRestView = likeService.getAllEventsLikedByUser(secondUser.getId());
        assertThat(likedEventsRestView.getLiked(), emptyIterable());
        assertThat(likedEventsRestView.getDisliked(), iterableWithSize(1));
        listOfShorts = new ArrayList<>(likedEventsRestView.getDisliked());
        assertThat(listOfShorts.get(0).getId(), equalTo(firstEvent.getId()));
    }

    @Test
    public void getAllUsersWhoLikedEventForInitiator_whenThereWereManyLikes_thenReturnWhoLikedRestView() {
        prepareBigDbForTest();
        WhoLikedRestView whoLikedRestView = likeService.getAllUsersWhoLikedEventForInitiator(
                users.get(0).getId(), events.get(0).getId(), false);

        assertThat(whoLikedRestView, notNullValue());
        assertThat(whoLikedRestView.getWhoLiked(), iterableWithSize(10));
        assertThat(whoLikedRestView.getWhoDisliked(), emptyIterable());

        whoLikedRestView = likeService.getAllUsersWhoLikedEventForInitiator(
                users.get(3).getId(), events.get(3).getId(), false);
        assertThat(whoLikedRestView.getWhoLiked(), iterableWithSize(5));
        assertThat(whoLikedRestView.getWhoDisliked(), iterableWithSize(2));

        whoLikedRestView = likeService.getAllUsersWhoLikedEventForInitiator(
                users.get(3).getId(), events.get(3).getId(), true);
        assertThat(whoLikedRestView.getWhoLiked(), emptyIterable());
        assertThat(whoLikedRestView.getWhoDisliked(), emptyIterable());
    }

    @Test
    public void getAllUsersWhoLikedEventForInitiator_whenNotInitiatorId_thenThrowException() {
        prepareLittleDbForTest();
        assertThrows(BadRequestParameterException.class, () ->
                likeService.getAllUsersWhoLikedEventForInitiator(firstUser.getId(), secondEvent.getId(), true));
    }

    @Test
    public void removeLikeByUser_whenCalled_changesRatings() {
        prepareLittleDbForTest();
        firstEvent = likeService.saveNewLike(LikeRestCommand.builder()
                .user(firstUser.getId())
                .event(firstEvent.getId())
                .isLike(true)
                .build());
        assertThat(firstEvent.getNumberOfLikes(), equalTo(2));
        assertThat(firstEvent.getRating(), equalTo(0));
        firstUser = userService.getUsersByIds(new long[] {firstUser.getId()}, 0, 10).get(0);
        assertThat(firstUser.getRating(), equalTo(0.0F));

        firstEvent = likeService.removeLikeByUser(secondUser.getId(), firstEvent.getId());
        assertThat(firstEvent.getNumberOfLikes(), equalTo(1));
        assertThat(firstEvent.getRating(), equalTo(1));
        firstUser = userService.getUsersByIds(new long[] {firstUser.getId()}, 0, 10).get(0);
        assertThat(firstUser.getRating(), equalTo(100.0F));

        secondEvent = likeService.removeLikeByUser(firstUser.getId(), secondEvent.getId());
        assertThat(secondEvent.getNumberOfLikes(), equalTo(0));
        assertThat(secondEvent.getRating(), equalTo(0));

        List<UserRestView> initiators = userService.getInitiatorsSortedByRating(0, 10, true);
        assertThat(initiators.get(0), equalTo(secondUser));
        assertThat(initiators.get(1), equalTo(firstUser));
    }

    @Test
    public void saveLikeByUser_whenCalledAgainAfterRemoving_thenReturnEventRestView() {
        prepareLittleDbForTest();
        likeService.removeLikeByUser(firstUser.getId(), secondEvent.getId());
        likeService.getAllEventsLikedByUser(firstUser.getId());
        secondEvent = eventService.getEventById(secondEvent.getId());
        assertThat(secondEvent.getRating(), equalTo(0));
        assertThat(secondEvent.getNumberOfLikes(), equalTo(0));

        secondEvent = likeService.saveNewLike(LikeRestCommand.builder()
                .user(firstUser.getId())
                .event(secondEvent.getId())
                .isLike(false)
                .build());
        assertThat(secondEvent.getRating(), equalTo(-1));
    }

    @ParameterizedTest
    @ValueSource(longs = { 0, -1 })
    public void allMethods_whenGetIncorrectNullOrNegativeValue_thenThrowException(long value) {
        prepareLittleDbForTest();

        assertThrows(ConstraintViolationException.class, () ->
                likeService.saveNewLike(LikeRestCommand.builder()
                        .user(value)
                        .event(firstEvent.getId())
                        .isLike(true)
                        .build()));
        assertThrows(ConstraintViolationException.class, () ->
                likeService.saveNewLike(LikeRestCommand.builder()
                        .user(firstUser.getId())
                        .event(value)
                        .isLike(true)
                        .build()));
        assertThrows(ConstraintViolationException.class, () ->
                likeService.getAllEventsLikedByUser(value));
        assertThrows(ConstraintViolationException.class, () ->
                likeService.getAllUsersWhoLikedEventForInitiator(value, secondEvent.getId(), true));
        assertThrows(ConstraintViolationException.class, () ->
                likeService.getAllUsersWhoLikedEventForInitiator(firstUser.getId(), value, true));
        assertThrows(ConstraintViolationException.class, () ->
                likeService.removeLikeByUser(value, secondEvent.getId()));
        assertThrows(ConstraintViolationException.class, () ->
                likeService.removeLikeByUser(firstUser.getId(), value));
        assertThrows(ConstraintViolationException.class, () ->
                userService.getInitiatorsSortedByRating(0, (int) value, true));
    }

    @Test
    public void allMethods_whenGetNotExistingId_throwObjectNotFoundException() {
        prepareLittleDbForTest();
        long notExistingUserId = secondUser.getId() + 1;
        long notExistingEventId = secondEvent.getId() + 1;

        assertThrows(ObjectNotFoundException.class, () ->
                likeService.saveNewLike(LikeRestCommand.builder()
                        .user(notExistingUserId)
                        .event(firstEvent.getId())
                        .isLike(true)
                        .build()));
        assertThrows(ObjectNotFoundException.class, () ->
                likeService.saveNewLike(LikeRestCommand.builder()
                        .user(firstUser.getId())
                        .event(notExistingEventId)
                        .isLike(true)
                        .build()));
        assertThrows(ObjectNotFoundException.class, () ->
                likeService.getAllEventsLikedByUser(notExistingUserId));
        assertThrows(ObjectNotFoundException.class, () ->
                likeService.getAllUsersWhoLikedEventForInitiator(notExistingUserId, firstEvent.getId(), true));
        assertThrows(ObjectNotFoundException.class, () ->
                likeService.getAllUsersWhoLikedEventForInitiator(secondUser.getId(), notExistingEventId, true));
        assertThrows(ObjectNotFoundException.class, () ->
                likeService.removeLikeByUser(notExistingUserId, secondEvent.getId()));
        assertThrows(ObjectNotFoundException.class, () ->
                likeService.removeLikeByUser(firstUser.getId(), notExistingEventId));
    }

    @Test
    public void likeRatingSystem_whenGetCorrectParameters_thenChangeRating() {
        prepareBigDbForTest();

        events = eventService.getAllEventsByParametersForAdmin(HttpAdminGetAllRequestParamsHolder.builder()
                .from(0)
                .size(10)
                .build());
        assertThat(events, iterableWithSize(5));
        assertThat(events.get(0).getTitle(), equalTo("title_1"));
        assertThat(events.get(0).getRating(), equalTo(10));
        assertThat(events.get(0).getNumberOfLikes(), equalTo(10));

        assertThat(events.get(1).getTitle(), equalTo("title_2"));
        assertThat(events.get(1).getRating(), equalTo(0));
        assertThat(events.get(1).getNumberOfLikes(), equalTo(10));

        assertThat(events.get(2).getTitle(), equalTo("title_3"));
        assertThat(events.get(2).getRating(), equalTo(-5));
        assertThat(events.get(2).getNumberOfLikes(), equalTo(5));

        assertThat(events.get(3).getTitle(), equalTo("title_4"));
        assertThat(events.get(3).getRating(), equalTo(3));
        assertThat(events.get(3).getNumberOfLikes(), equalTo(7));

        assertThat(events.get(4).getTitle(), equalTo("title_5"));
        assertThat(events.get(4).getRating(), equalTo(0));
        assertThat(events.get(4).getNumberOfLikes(), equalTo(0));

        List<EventRestViewShort> sortedEvents = eventService.getAllEventsByParametersForAnyone(
                HttpPublicGetAllRequestParamsHolder.builder()
                        .sort(SortBy.RATING.name())
                        .from(0)
                        .size(10)
                        .build());
        assertThat(sortedEvents, iterableWithSize(5));
        assertThat(sortedEvents.get(0).getTitle(), equalTo("title_1"));
        assertThat(sortedEvents.get(0).getRating(), equalTo(10));

        assertThat(sortedEvents.get(1).getTitle(), equalTo("title_4"));
        assertThat(sortedEvents.get(1).getRating(), equalTo(3));

        assertThat(sortedEvents.get(2).getTitle(), equalTo("title_2"));
        assertThat(sortedEvents.get(2).getRating(), equalTo(0));

        assertThat(sortedEvents.get(3).getTitle(), equalTo("title_5"));
        assertThat(sortedEvents.get(3).getRating(), equalTo(0));

        assertThat(sortedEvents.get(4).getTitle(), equalTo("title_3"));
        assertThat(sortedEvents.get(4).getRating(), equalTo(-5));

        List<UserRestView> sortedUsers = userService.getInitiatorsSortedByRating(0, 10, false);
        assertThat(sortedUsers, iterableWithSize(5));
        assertThat(sortedUsers.get(0).getName(), equalTo("user_1"));
        assertThat(sortedUsers.get(0).getRating(), equalTo(100.0F));

        assertThat(sortedUsers.get(1).getName(), equalTo("user_4"));
        assertThat(sortedUsers.get(1).getRating(), lessThan(42.9F));
        assertThat(sortedUsers.get(1).getRating(), greaterThan(42.8F));

        assertThat(sortedUsers.get(2).getName(), equalTo("user_2"));
        assertThat(sortedUsers.get(2).getRating(), equalTo(0F));

        assertThat(sortedUsers.get(3).getName(), equalTo("user_5"));
        assertThat(sortedUsers.get(3).getRating(), equalTo(0F));

        assertThat(sortedUsers.get(4).getName(), equalTo("user_3"));
        assertThat(sortedUsers.get(4).getRating(), equalTo(-100.0F));

        EventRestView newEvent = eventService.saveNewEvent(users.get(0).getId(), EventRestCommand.builder()
                .title("title")
                .annotation("annotation_of_new_event_for_test")
                .description("description_of_new_event_for_test")
                .eventDate(DEFAULT_EVENT_DATE.format(EwmConstants.FORMATTER))
                .category(categoryOne.getId())
                .location(DEFAULT_LOCATION)
                .build());
        eventService.updateEventFromAdmin(newEvent.getId(), EventRestCommand.builder()
                .stateAction(StateAction.PUBLISH_EVENT.name())
                .build());
        likeService.saveNewLike(LikeRestCommand.builder()
                .user(users.get(2).getId())
                .event(newEvent.getId())
                .isLike(false)
                .build());
        likeService.saveNewLike(LikeRestCommand.builder()
                .user(users.get(3).getId())
                .event(newEvent.getId())
                .isLike(false)
                .build());

        sortedEvents = eventService.getAllEventsByParametersForAnyone(HttpPublicGetAllRequestParamsHolder.builder()
                        .sort(SortBy.RATING.name())
                        .from(0)
                        .size(10)
                        .build());
        assertThat(sortedEvents, iterableWithSize(6));
        assertThat(sortedEvents.get(4).getTitle(), equalTo("title"));
        assertThat(sortedEvents.get(4).getRating(), equalTo(-2));
        assertThat(sortedEvents.get(5).getTitle(), equalTo("title_3"));
        assertThat(sortedEvents.get(5).getRating(), equalTo(-5));

        sortedUsers = userService.getInitiatorsSortedByRating(0, 10, false);
        assertThat(sortedUsers, iterableWithSize(5));
        assertThat(sortedUsers.get(0).getName(), equalTo("user_1"));
        assertThat(sortedUsers.get(0).getRating(), lessThan(67.0F));
        assertThat(sortedUsers.get(0).getRating(), greaterThan(66.6F));

        users.forEach(user -> {
            if (user.getId() % 2 == 0 && user.getId() != users.get(9).getId()) {
                likeService.removeLikeByUser(user.getId(), events.get(2).getId());
            }
        });

        sortedEvents = eventService.getAllEventsByParametersForAnyone(HttpPublicGetAllRequestParamsHolder.builder()
                .sort(SortBy.RATING.name())
                .from(0)
                .size(10)
                .build());
        assertThat(sortedEvents, iterableWithSize(6));
        assertThat(sortedEvents.get(4).getTitle(), equalTo("title_3"));
        assertThat(sortedEvents.get(4).getRating(), equalTo(-1));
        assertThat(sortedEvents.get(5).getTitle(), equalTo("title"));
        assertThat(sortedEvents.get(5).getRating(), equalTo(-2));
    }

    private void prepareBigDbForTest() {
        List<UserRestCommand> userRestCommands = new ArrayList<>();
        List<EventRestCommand> eventRestCommands = new ArrayList<>();

        categoryOne = categoryService.saveNewCategory(initializeNewCategory("categoryOne"));
        CategoryRestView categoryTwo = categoryService.saveNewCategory(initializeNewCategory("categoryTwo"));

        for (int i = 1; i < 11; i++) {
            userRestCommands.add(UserRestCommand.builder()
                    .name("user_" + i)
                    .email("user_" + i + "@email.com")
                    .build());
            if (i <= 5) {
                if (i % 2 != 0) {
                    eventRestCommands.add(EventRestCommand.builder()
                            .title("title_" + i)
                            .annotation("annotation_of_" + i + "_event")
                            .description("description_of_" + i + "_like")
                            .eventDate(DEFAULT_EVENT_DATE.format(EwmConstants.FORMATTER))
                            .category(categoryOne.getId())
                            .location(DEFAULT_LOCATION)
                            .paid(true)
                            .requestModeration(false)
                            .participantLimit(10)
                            .build());
                } else {
                    eventRestCommands.add(EventRestCommand.builder()
                            .title("title_" + i)
                            .annotation("annotation_of_" + i + "_event")
                            .description("description_of_" + i + "_like")
                            .eventDate(DEFAULT_EVENT_DATE.format(EwmConstants.FORMATTER))
                            .category(categoryTwo.getId())
                            .location(DEFAULT_LOCATION)
                            .build());
                }
            }
            UserRestView user = userService.saveNewUser(userRestCommands.get(i - 1));
            users.add(user);
            if (i <= 5) {
                EventRestView event = eventService.saveNewEvent(user.getId(), eventRestCommands.get(i - 1));
                events.add(event);
            }
        }
        events.forEach(eventRestView -> eventService.updateEventFromAdmin(eventRestView.getId(), EventRestCommand
                .builder()
                .stateAction(StateAction.PUBLISH_EVENT.name())
                .build()));
        events = eventService.getAllEventsByParametersForAdmin(HttpAdminGetAllRequestParamsHolder.builder()
                .from(0)
                .size(10)
                .build());
        users.forEach(user -> likeService.saveNewLike(LikeRestCommand.builder()
                .user(user.getId())
                .event(events.get(0).getId())
                .isLike(true)
                .build()));
        users.forEach(user -> {
            if (user.getId() % 2 == 0) {
                likeService.saveNewLike(LikeRestCommand.builder()
                        .user(user.getId())
                        .event(events.get(1).getId())
                        .isLike(true)
                        .build());
            } else {
                likeService.saveNewLike(LikeRestCommand.builder()
                        .user(user.getId())
                        .event(events.get(1).getId())
                        .isLike(false)
                        .build());
            }
        });
        users.forEach(user -> {
            if (user.getId() % 2 == 0) {
                likeService.saveNewLike(LikeRestCommand.builder()
                        .user(user.getId())
                        .event(events.get(2).getId())
                        .isLike(false)
                        .build());
            }
        });
        users.forEach(user -> {
            if (user.getId() <= users.get(6).getId()) {
                if (user.getId() <= users.get(4).getId()) {
                    likeService.saveNewLike(LikeRestCommand.builder()
                            .user(user.getId())
                            .event(events.get(3).getId())
                            .isLike(true)
                            .build());
                } else {
                    likeService.saveNewLike(LikeRestCommand.builder()
                            .user(user.getId())
                            .event(events.get(3).getId())
                            .isLike(false)
                            .build());
                }
            }
        });
    }

    private void prepareLittleDbForTest() {
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
        categoryOne = categoryService.saveNewCategory(categoryRestCommand);

        assertThat(firstUser, notNullValue());
        assertTrue(firstUser.getId() >= 1L);
        assertThat(secondUser, notNullValue());
        assertTrue(secondUser.getId() >= 2L);
        assertThat(categoryOne, notNullValue());
        assertTrue(categoryOne.getId() >= 1L);

        firstEvent = eventService.saveNewEvent(firstUser.getId(), EventRestCommand.builder()
                .title("title_1")
                .annotation("annotation_of_first_event")
                .description("description_of_first_event")
                .eventDate(DEFAULT_EVENT_DATE.format(EwmConstants.FORMATTER))
                .category(categoryOne.getId())
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
                .category(categoryOne.getId())
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
        likeService.saveNewLike(LikeRestCommand.builder()
                .user(firstUser.getId())
                .event(secondEvent.getId())
                .isLike(true)
                .build());
        likeService.saveNewLike(LikeRestCommand.builder()
                .user(secondUser.getId())
                .event(firstEvent.getId())
                .isLike(false)
                .build());
    }

    private CategoryRestCommand initializeNewCategory(String name) {
        CategoryRestCommand categoryRestCommand = new CategoryRestCommand();
        categoryRestCommand.setName(name);
        return categoryRestCommand;
    }

}