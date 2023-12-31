package ru.practicum.explore_with_me.main_service.controller.for_autorized;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import ru.practicum.explore_with_me.main_service.controller.for_authorized.RequestController;
import ru.practicum.explore_with_me.main_service.mapper.RequestMapper;
import ru.practicum.explore_with_me.main_service.mapper.RequestMapperImpl;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.request.RequestStatus;
import ru.practicum.explore_with_me.main_service.model.rest_dto.request.RequestRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.request.RequestRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.request.RequestStatusSetRestCommand;
import ru.practicum.explore_with_me.main_service.service.*;
import ru.practicum.explore_with_me.stats_service.client_submodule.StatsClient;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.EwmConstants;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.HitRestView;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {RequestController.class})
@ContextConfiguration(classes = { RequestController.class })
public class RequestControllerTest {
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    RequestService requestService;
    @MockBean
    StatsClient statsClient;
    @Autowired
    private MockMvc mvc;
    private final RequestMapper requestMapper = new RequestMapperImpl();

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

    @Test
    public void getAllRequestsByUserId_whenGetCorrectParameter_thenReturnListOfRequestRestView() throws Exception {
        when(requestService.getAllRequestsOfUser(Mockito.anyLong()))
                .thenReturn(List.of(request));

        mvc.perform(get("/users/555/requests")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(objectMapper.readValue(
                        objectMapper.writeValueAsString(List.of(request)), List.class))));

        verify(requestService, Mockito.times(1))
                .getAllRequestsOfUser(Mockito.anyLong());
    }

    @Test
    public void getAllRequestsToEventForInitiator_whenGetCorrectParameters_thenReturnListOfRequestRestView() throws Exception {
        when(requestService.getAllRequestsToEventForInitiator(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(List.of(request));

        mvc.perform(get("/users/555/events/777/requests")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(objectMapper.readValue(
                        objectMapper.writeValueAsString(List.of(request)), List.class))));

        verify(requestService, Mockito.times(1))
                .getAllRequestsToEventForInitiator(Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    public void cancelRequest_whenGetCorrectParameters_thenReturnRequestRestView() throws Exception {
        when(requestService.cancelRequestByRequester(Mockito.anyLong(), Mockito.any(BigInteger.class)))
                .thenReturn(request);

        mvc.perform(patch("/users/77777777/requests/5555555555555/cancel")
                        .content(objectMapper.writeValueAsString(RequestRestCommand.builder().build()))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id", is(BigInteger.ONE), BigInteger.class))
                .andExpect(jsonPath("$.requester", is(1L), Long.class))
                .andExpect(jsonPath("$.event", is(2L), Long.class))
                .andExpect(jsonPath("$.status", is("CONFIRMED")))
                .andExpect(jsonPath("$.created",
                        is(EwmConstants.DEFAULT_DATE_TIME.toString())));

        verify(requestService, Mockito.times(1))
                .cancelRequestByRequester(Mockito.anyLong(), Mockito.any(BigInteger.class));
    }

    @Test
    public void setStatusToRequestsByInitiator_whenReceiveCorrectParameters_thenReturnRequestRestView() throws Exception {
        when(requestService.setStatusToRequestsByInitiator(Mockito.anyLong(), Mockito.anyLong(),
                Mockito.any(RequestStatusSetRestCommand.class)))
                .thenReturn(requestMapper.mapModeratedRequestsRestViewFromListOfRequests(List.of(request)));
        when(statsClient.addNewHit(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(ResponseEntity.ok(HitRestView.builder().build()));

        mvc.perform(patch("/users/7777/events/1234567/requests")
                        .content(objectMapper.writeValueAsBytes(RequestStatusSetRestCommand.builder().build()))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmedRequests", is(objectMapper.readValue(
                        objectMapper.writeValueAsString(List.of(request)), List.class))))
                .andExpect(jsonPath("$.rejectedRequests", is(objectMapper.readValue(
                        objectMapper.writeValueAsString(Collections.emptyList()), List.class))));

        verify(requestService, Mockito.times(1))
                .setStatusToRequestsByInitiator(Mockito.anyLong(), Mockito.anyLong(),
                        Mockito.any(RequestStatusSetRestCommand.class));
    }

}