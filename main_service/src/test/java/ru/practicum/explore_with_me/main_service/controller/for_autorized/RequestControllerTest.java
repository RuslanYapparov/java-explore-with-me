package ru.practicum.explore_with_me.main_service.controller.for_autorized;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explore_with_me.main_service.controller.for_authorized.RequestController;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.request.RequestStatus;
import ru.practicum.explore_with_me.main_service.model.rest_dto.request.RequestRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.request.RequestRestView;
import ru.practicum.explore_with_me.main_service.service.CategoryService;
import ru.practicum.explore_with_me.main_service.service.EventService;
import ru.practicum.explore_with_me.main_service.service.RequestService;
import ru.practicum.explore_with_me.main_service.service.UserService;
import ru.practicum.explore_with_me.stats_service.client_submodule.StatsClient;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.EwmConstants;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {RequestController.class})
public class RequestControllerTest {
    @MockBean
    RequestService requestService;
    @MockBean
    CategoryService categoryService;
    @MockBean
    UserService userService;
    @MockBean
    EventService eventService;
    @MockBean
    StatsClient statsClient;
    @Autowired
    private MockMvc mvc;

    private final RequestRestView request = RequestRestView.builder()
            .id(BigInteger.ONE)
            .requester(1L)
            .event(2L)
            .createdOn(EwmConstants.DEFAULT_DATE_TIME)
            .status(RequestStatus.CONFIRMED.name())
            .build();

    @Test
    public void saveNewRequest_whenGetCorrectParameters_thenReturnRequestRestView() throws Exception {
        when(requestService.saveNewRequest(Mockito.any(RequestRestCommand.class)))
                .thenReturn(request);

        mvc.perform(post("/users/2/requests")
                        .param("eventId", "3")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(BigInteger.ONE), BigInteger.class))
                .andExpect(jsonPath("$.requester", is(1L), Long.class))
                .andExpect(jsonPath("$.event", is(2L), Long.class))
                .andExpect(jsonPath("$.status", is("CONFIRMED")))
                .andExpect(jsonPath("$.created",
                        is(EwmConstants.DEFAULT_DATE_TIME.toString())));

        verify(requestService, Mockito.times(1))
                .saveNewRequest(Mockito.any(RequestRestCommand.class));
    }

}