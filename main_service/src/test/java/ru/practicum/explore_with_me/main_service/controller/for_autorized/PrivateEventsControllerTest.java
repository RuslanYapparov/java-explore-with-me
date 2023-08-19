package ru.practicum.explore_with_me.main_service.controller.for_autorized;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explore_with_me.main_service.controller.admin.AdminEventsController;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.event.EventState;
import ru.practicum.explore_with_me.main_service.model.rest_dto.category.CategoryRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.*;
import ru.practicum.explore_with_me.main_service.model.rest_dto.user.UserShort;
import ru.practicum.explore_with_me.main_service.service.*;
import ru.practicum.explore_with_me.stats_service.client_submodule.StatsClient;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.EwmConstants;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AdminEventsController.class})
public class PrivateEventsControllerTest {
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
    public void saveNewEvent_whenGetCorrectParameters_thenReturnEventRestView() throws Exception {
        when(eventService.saveNewEvent(Mockito.anyLong(), Mockito.any(EventRestCommand.class)))
                .thenReturn(event);

        mvc.perform(post("/users/1/events")
                        .content(objectMapper.writeValueAsString(EventRestCommand.builder().build()))
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

        verify(eventService, Mockito.times(1))
                .saveNewEvent(Mockito.anyLong(), Mockito.any(EventRestCommand.class));
    }

    @Test
    public void getAllEventsByUserId_whenGetOnlyPathVariable_thenReturnListOfEventRestView() throws Exception {
        when(eventService.getAllEventsByUserId(Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(List.of(eventShort));

        mvc.perform(get("/users/555/events")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(objectMapper.readValue(
                        objectMapper.writeValueAsString(List.of(eventShort)), List.class))));

        verify(eventService, Mockito.times(1))
                .getAllEventsByUserId(Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    public void getEventOfUserById_whenGetOnlyPathVariable_thenReturnListOfEventRestView() throws Exception {
        when(eventService.getEventOfUserById(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(event);

        mvc.perform(get("/users/555/events/777")
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
                .getEventOfUserById(Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    public void updateEvent_whenGetCorrectParameters_thenReturnEventRestView() throws Exception {
        when(eventService.updateEventFromInitiator(Mockito.anyLong(), Mockito.anyLong(),
                Mockito.any(EventRestCommand.class))).thenReturn(event);

        mvc.perform(patch("/users/77777777/events/5555555555555")
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
                .updateEventFromInitiator(Mockito.anyLong(), Mockito.anyLong(), Mockito.any(EventRestCommand.class));
    }

}