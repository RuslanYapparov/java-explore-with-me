package ru.practicum.explore_with_me.main_service.controller.for_autorized;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import ru.practicum.explore_with_me.main_service.controller.for_authorized.LikeController;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.event.EventState;
import ru.practicum.explore_with_me.main_service.model.rest_dto.category.CategoryRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.EventRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.EventRestViewShort;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.GeoLocation;
import ru.practicum.explore_with_me.main_service.model.rest_dto.like.LikeRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.like.LikedEventsRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.like.WhoLikedRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.user.UserRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.user.UserShort;
import ru.practicum.explore_with_me.main_service.service.*;
import ru.practicum.explore_with_me.stats_service.client_submodule.StatsClient;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.EwmConstants;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = { LikeController.class })
@ContextConfiguration(classes = { LikeController.class })
public class LikeControllerTest {
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    LikeService likeService;
    @MockBean
    StatsClient statsClient;
    @Autowired
    private MockMvc mvc;

    private final EventRestView event = EventRestView.builder()
            .id(1L)
            .title("title")
            .annotation("annotation")
            .description("description")
            .state(EventState.PENDING.name())
            .category(CategoryRestView.builder()
                    .id(2L)
                    .name("category")
                    .build())
            .initiator(UserShort.builder()
                    .id(3L)
                    .name("user")
                    .build())
            .location(GeoLocation.builder()
                    .latitude(5.5D)
                    .longitude(7.7D)
                    .build())
            .confirmedRequests(5)
            .participantLimit(7)
            .eventDate(EwmConstants.DEFAULT_DATE_TIME.plusYears(1L))
            .createdOn(EwmConstants.DEFAULT_DATE_TIME)
            .paid(false)
            .requestModeration(true)
            .views(777)
            .build();
    private final EventRestViewShort eventShort = EventRestViewShort.builder()
            .id(1L)
            .title("title")
            .annotation("annotation")
            .category(CategoryRestView.builder()
                    .id(2L)
                    .name("category")
                    .build())
            .initiator(UserShort.builder()
                    .id(3L)
                    .name("user")
                    .build())
            .confirmedRequests(5)
            .eventDate(EwmConstants.DEFAULT_DATE_TIME.plusYears(1L))
            .paid(false)
            .views(777)
            .build();

    @Test
    public void saveNewLike_whenGetCorrectParameters_thenReturnEventRestView() throws Exception {
        when(likeService.saveNewLike(Mockito.any(LikeRestCommand.class)))
                .thenReturn(event);

        mvc.perform(post("/users/2/likes")
                        .param("eventId", "3")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1L), Long.class))
                .andExpect(jsonPath("$.title", is("title")))
                .andExpect(jsonPath("$.annotation", is("annotation")))
                .andExpect(jsonPath("$.description", is("description")))
                .andExpect(jsonPath("$.state", is("PENDING")))
                .andExpect(jsonPath("$.category.id", is(2L), Long.class))
                .andExpect(jsonPath("$.category.name", is("category")))
                .andExpect(jsonPath("$.initiator.id", is(3L), Long.class))
                .andExpect(jsonPath("$.initiator.name", is("user")))
                .andExpect(jsonPath("$.location.lat", is(5.5D), Double.class))
                .andExpect(jsonPath("$.location.lon", is(7.7D), Double.class))
                .andExpect(jsonPath("$.confirmedRequests", is(5), Integer.class))
                .andExpect(jsonPath("$.participantLimit", is(7), Integer.class))
                .andExpect(jsonPath("$.eventDate",
                        is(EwmConstants.DEFAULT_DATE_TIME.plusYears(1).format(EwmConstants.FORMATTER))))
                .andExpect(jsonPath("$.createdOn",
                        is(EwmConstants.DEFAULT_DATE_TIME.format(EwmConstants.FORMATTER))))
                .andExpect(jsonPath("$.paid", is(false), Boolean.class))
                .andExpect(jsonPath("$.requestModeration", is(true), Boolean.class))
                .andExpect(jsonPath("$.views", is(777L), Long.class));

        verify(likeService, Mockito.times(1))
                .saveNewLike(Mockito.any(LikeRestCommand.class));
    }

    @Test
    public void getAllEventsLikedByUser_whenGetCorrectParameter_thenReturnLikedEventsRestView() throws Exception {
        when(likeService.getAllEventsLikedByUser(Mockito.anyLong()))
                .thenReturn(LikedEventsRestView.builder()
                        .liked(new HashSet<>(List.of(eventShort)))
                        .build());

        mvc.perform(get("/users/555/likes")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likedEvents", is(objectMapper.readValue(
                        objectMapper.writeValueAsString(List.of(eventShort)), List.class))))
                .andExpect(jsonPath("$.dislikedEvents", is(nullValue())));

        verify(likeService, Mockito.times(1))
                .getAllEventsLikedByUser(Mockito.anyLong());
    }

    @Test
    public void getAllUsersWhoLikedEventForInitiator_whenGetCorrectParameters_thenReturnWhoLikedRestView()
            throws Exception {
        UserRestView user = UserRestView.builder()
                .id(1L)
                .name("user")
                .email("email")
                .rating(100.0F)
                .build();
        when(likeService.getAllUsersWhoLikedEventForInitiator(
                Mockito.anyLong(), Mockito.anyLong(), Mockito.anyBoolean()))
                .thenReturn(WhoLikedRestView.builder()
                        .whoLiked(new HashSet<>(List.of(user)))
                        .build());
        mvc.perform(get("/users/555/events/777/likes")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.whoLiked", is(objectMapper.readValue(
                        objectMapper.writeValueAsString(List.of(user)), List.class))))
                .andExpect(jsonPath("$.whoDisliked", is(nullValue())));


        verify(likeService, Mockito.times(1))
                .getAllUsersWhoLikedEventForInitiator(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyBoolean());
    }

    @Test
    public void removeLike_whenGetCorrectParameters_thenReturnEventRestView() throws Exception {
        when(likeService.removeLikeByUser(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(event);

        mvc.perform(delete("/users/77777777/likes/5555555555555/remove")
                        .content(objectMapper.writeValueAsString(LikeRestCommand.builder().build()))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.id", is(1L), Long.class))
                .andExpect(jsonPath("$.title", is("title")))
                .andExpect(jsonPath("$.annotation", is("annotation")))
                .andExpect(jsonPath("$.description", is("description")))
                .andExpect(jsonPath("$.state", is("PENDING")))
                .andExpect(jsonPath("$.category.id", is(2L), Long.class))
                .andExpect(jsonPath("$.category.name", is("category")))
                .andExpect(jsonPath("$.initiator.id", is(3L), Long.class))
                .andExpect(jsonPath("$.initiator.name", is("user")))
                .andExpect(jsonPath("$.location.lat", is(5.5D), Double.class))
                .andExpect(jsonPath("$.location.lon", is(7.7D), Double.class))
                .andExpect(jsonPath("$.confirmedRequests", is(5), Integer.class))
                .andExpect(jsonPath("$.participantLimit", is(7), Integer.class))
                .andExpect(jsonPath("$.eventDate",
                        is(EwmConstants.DEFAULT_DATE_TIME.plusYears(1).format(EwmConstants.FORMATTER))))
                .andExpect(jsonPath("$.createdOn",
                        is(EwmConstants.DEFAULT_DATE_TIME.format(EwmConstants.FORMATTER))))
                .andExpect(jsonPath("$.paid", is(false), Boolean.class))
                .andExpect(jsonPath("$.requestModeration", is(true), Boolean.class))
                .andExpect(jsonPath("$.views", is(777L), Long.class));

        verify(likeService, Mockito.times(1))
                .removeLikeByUser(Mockito.anyLong(), Mockito.anyLong());
    }

}