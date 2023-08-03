package ru.practicum.explore_with_me.stats_service.server_submodule;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explore_with_me.stats_service.client_submodule.client.StatsClient;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.HitRestCommand;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.HitRestView;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.UriStatRestView;
import ru.practicum.explore_with_me.stats_service.server_submodule.controller.StatsController;
import ru.practicum.explore_with_me.stats_service.server_submodule.service.StatsService;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StatsController.class)
public class StatsControllerTest {
    private static final LocalDateTime DEFAULT_DATE_TIME = LocalDateTime.of(2023,8,1, 0, 0, 1);
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    StatsService statsService;
    @Autowired
    private MockMvc mvc;

    private final HitRestView hitRestView = HitRestView.builder()
            .id(BigInteger.ONE)
            .application("application")
            .ip("ip")
            .uri("uri")
            .timestamp(DEFAULT_DATE_TIME)
            .build();
    private final UriStatRestView uriStatRestView = UriStatRestView.builder()
            .uri("uri")
            .application("application")
            .hits(1)
            .build();

    @Test
    public void saveNewHit_whenGetCorrectHitRestCommand_thenReturnHitRestView() throws Exception {
        when(statsService.saveHit(Mockito.any(HitRestCommand.class)))
                .thenReturn(hitRestView);

        mvc.perform(post("/hit")
                .content(objectMapper.writeValueAsString(HitRestCommand.builder()
                        .application("app")
                        .ip("ip")
                        .uri("uri")
                        .timestamp(DEFAULT_DATE_TIME.format(StatsClient.FORMATTER))
                        .build()))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(hitRestView.getId()), BigInteger.class))
                .andExpect(jsonPath("$.app", is(hitRestView.getApplication())))
                .andExpect(jsonPath("$.ip", is(hitRestView.getIp())))
                .andExpect(jsonPath("$.uri", is(hitRestView.getUri())))
                .andExpect(jsonPath("$.timestamp", is(DEFAULT_DATE_TIME.format(StatsClient.FORMATTER))));

        verify(statsService, Mockito.times(1))
                .saveHit(Mockito.any(HitRestCommand.class));
    }

    @Test
    public void getUriStats_whenGetCorrectHitRestCommand_thenReturnHitRestView() throws Exception {
        when(statsService.getAllUriStatsOrderedByHits(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.any(String[].class),
                Mockito.anyBoolean()))
                .thenReturn(List.of(uriStatRestView));

        mvc.perform(get("/stats")
                        .param("start", "start")
                        .param("end", "end")
                        .param("uris", "uri1", "uri2", "uri3")
                        .param("unique", "true")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(objectMapper.readValue(objectMapper.writeValueAsString(
                        List.of(uriStatRestView)), List.class))));

        verify(statsService, Mockito.times(1))
                .getAllUriStatsOrderedByHits(Mockito.anyString(),
                        Mockito.anyString(),
                        Mockito.any(String[].class),
                        Mockito.anyBoolean());
    }

}