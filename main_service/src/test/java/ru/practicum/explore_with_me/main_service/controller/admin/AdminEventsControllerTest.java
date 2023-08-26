package ru.practicum.explore_with_me.main_service.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explore_with_me.main_service.controller.ExploreWithMeExceptionHandler;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.event.EventState;
import ru.practicum.explore_with_me.main_service.model.rest_dto.category.CategoryRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.EventRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.EventRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.GeoLocation;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.HttpAdminGetAllRequestParamsHolder;
import ru.practicum.explore_with_me.main_service.model.rest_dto.user.UserShort;
import ru.practicum.explore_with_me.main_service.service.*;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.EwmConstants;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = { AdminEventsController.class })
@ContextConfiguration(classes = { AdminEventsController.class, ExploreWithMeExceptionHandler.class })
public class AdminEventsControllerTest {
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    EventService eventService;
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

    @Test
    public void getAllEventsByParameter_whenDoNotReceiveParameters_thenReturnListOfEventRestView() throws Exception {
        when(eventService.getAllEventsByParametersForAdmin(Mockito.any(HttpAdminGetAllRequestParamsHolder.class)))
                .thenReturn(List.of(event));

        mvc.perform(get("/admin/events")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(objectMapper.readValue(
                        objectMapper.writeValueAsString(List.of(event)), List.class))));

        verify(eventService, Mockito.times(1))
                .getAllEventsByParametersForAdmin(Mockito.any(HttpAdminGetAllRequestParamsHolder.class));
    }

    @Test
    public void updateEvent_whenGetCorrectParameters_thenReturnEventRestView() throws Exception {
        when(eventService.updateEventFromAdmin(Mockito.anyLong(), Mockito.any(EventRestCommand.class)))
                .thenReturn(event);

        mvc.perform(patch("/admin/events/1")
                        .content(objectMapper.writeValueAsString(EventRestCommand.builder().build()))
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
                .updateEventFromAdmin(Mockito.anyLong(), Mockito.any(EventRestCommand.class));
    }

    @Test
    public void updateEvent_whenGetNullOrIncorrectRequestBody_thenThrowException() throws Exception {
        when(eventService.updateEventFromAdmin(Mockito.anyLong(), Mockito.any(EventRestCommand.class)))
                .thenReturn(event);

        mvc.perform(patch("/admin/events/1")
                        .content("")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is(
                        "Failed to read request body (content is incorrect).")))
                .andExpect(jsonPath("$.message", is("Required request body is missing: public " +
                        "ru.practicum.explore_with_me.main_service.model.rest_dto.event.EventRestView " +
                        "ru.practicum.explore_with_me.main_service.controller.admin.AdminEventsController.updateEvent" +
                        "(long,ru.practicum.explore_with_me.main_service.model.rest_dto.event.EventRestCommand)")));

        mvc.perform(patch("/admin/events/1")
                        .content("potato")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is(
                        "Failed to read request body (content is incorrect).")))
                .andExpect(jsonPath("$.message", is("JSON parse error: Unrecognized token 'potato': " +
                        "was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false'); " +
                        "nested exception is com.fasterxml.jackson.core.JsonParseException: Unrecognized token 'potato':" +
                        " was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')\n" +
                        " at [Source: (org.springframework.util.StreamUtils$NonClosingInputStream); " +
                        "line: 1, column: 7]")));

        verify(eventService, Mockito.never())
                .updateEventFromAdmin(Mockito.anyLong(), Mockito.any(EventRestCommand.class));
    }

}