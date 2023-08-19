package ru.practicum.explore_with_me.main_service.controller.for_all;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import ru.practicum.explore_with_me.main_service.model.domain_pojo.event.EventState;
import ru.practicum.explore_with_me.main_service.model.rest_dto.category.CategoryRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.*;
import ru.practicum.explore_with_me.main_service.model.rest_dto.user.UserShort;
import ru.practicum.explore_with_me.main_service.service.*;
import ru.practicum.explore_with_me.stats_service.client_submodule.StatsClient;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.EwmConstants;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.HitRestView;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {PublicEventController.class})
public class PublicEventsControllerTest {
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    EventService eventService;
    @MockBean
    CategoryService categoryService;
    @MockBean
    UserService userService;
    @MockBean
    RequestService requestService;
    @MockBean
    CompilationService compilationService;
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
    public void getEventById_whenReceiveCorrectParameters_thenReturnEventRestView() throws Exception {
        when(eventService.getEventById(Mockito.anyLong()))
                .thenReturn(event);
        when(statsClient.addNewHit(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(ResponseEntity.ok(HitRestView.builder().build()));

        mvc.perform(get("/events/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
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

        verify(eventService, Mockito.times(1))
                .getEventById(Mockito.anyLong());
        verify(statsClient, Mockito.times(1))
                .addNewHit(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void getAllEventsByParameters_whenDoNotReceiveParameters_thenReturnListOfEventRestView() throws Exception {
        when(eventService.getAllEventsByParametersForAnyone(Mockito.any(HttpPublicGetAllRequestParamsHolder.class)))
                .thenReturn(List.of(eventShort));
        when(statsClient.addNewHit(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(ResponseEntity.ok(HitRestView.builder().build()));


        mvc.perform(get("/events")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(objectMapper.readValue(
                        objectMapper.writeValueAsString(List.of(eventShort)), List.class))));

        verify(eventService, Mockito.times(1))
                .getAllEventsByParametersForAnyone(Mockito.any(HttpPublicGetAllRequestParamsHolder.class));
        verify(statsClient, Mockito.times(1))
                .addNewHit(Mockito.anyString(), Mockito.anyString());
    }



}