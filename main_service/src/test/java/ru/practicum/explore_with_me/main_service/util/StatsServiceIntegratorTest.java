package ru.practicum.explore_with_me.main_service.util;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.ResponseEntity;
import ru.practicum.explore_with_me.main_service.exception.StatsServiceProblemException;
import ru.practicum.explore_with_me.stats_service.client_submodule.StatsClient;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.HitRestView;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.UriStatRestView;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class StatsServiceIntegratorTest {
    private final StatsServiceIntegrator integrator;
    @MockBean
    StatsClient statsClient;

    @Test
    public void saveStatHitFromThisRequests_whenGetNullHitUri_thenThrowException() {
        when(statsClient.addNewHit(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(ResponseEntity.badRequest().body(null));

        StatsServiceProblemException exception = assertThrows(StatsServiceProblemException.class, () ->
                integrator.saveStatHitFromThisRequests("ip", "uri"));
        assertThat(exception.getMessage(), equalTo("Failed to get data from Stats_sever: there is null hit body " +
                "in the response from server"));
    }

    @Test
    public void saveStatHitFromThisRequests_whenGetHitUriWithCode400_thenThrowException() {
        when(statsClient.addNewHit(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(ResponseEntity.badRequest().body(HitRestView.builder()
                        .application("Wow! There is exception in Stats_service!")
                        .build()));

        StatsServiceProblemException exception = assertThrows(StatsServiceProblemException.class, () ->
                integrator.saveStatHitFromThisRequests("ip", "uri"));
        assertThat(exception.getMessage(), equalTo("Wow! There is exception in Stats_service!"));
    }

    @Test
    public void getUriStatsFromService_whenGetNullUriStats_thenThrowException() {
        when(statsClient.getUriStats(Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class),
                Mockito.any(String[].class), Mockito.anyBoolean()))
                .thenReturn(ResponseEntity.badRequest().body(null));

        StatsServiceProblemException exception = assertThrows(StatsServiceProblemException.class, () ->
                integrator.getUriStatsFromService(new String[] {}));
        assertThat(exception.getMessage(), equalTo("Failed to get data from Stats_sever: there is " +
                "null statistics array in the response from server"));
    }

    @Test
    public void getUriStatsFromService_whenGetUriStatWithCode400_thenThrowException() {
        when(statsClient.getUriStats(Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class),
                Mockito.any(String[].class), Mockito.anyBoolean()))
                .thenReturn(ResponseEntity.badRequest().body(new UriStatRestView[] {UriStatRestView.builder()
                        .application("Wow! There is exception in Stats_service!")
                        .build()}));

        StatsServiceProblemException exception = assertThrows(StatsServiceProblemException.class, () ->
                integrator.getViewsForOneUri("uri"));
        assertThat(exception.getMessage(), equalTo("Wow! There is exception in Stats_service!"));
    }

    @Test
    public void getViewsForOneUri_whenGetUriStatWithIncorrectUri_thenThrowException() {
        String uri = "correct_uri";
        ResponseEntity<UriStatRestView[]> response = ResponseEntity.ok().body(new UriStatRestView[] {UriStatRestView.builder()
                .uri("incorrect_uri")
                .build()});
        when(statsClient.getUriStats(Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class),
                Mockito.any(String[].class), Mockito.anyBoolean()))
                .thenReturn(response);

        StatsServiceProblemException exception = assertThrows(StatsServiceProblemException.class, () ->
                integrator.getViewsForOneUri(uri));
        assertThat(exception.getMessage(), equalTo(String.format("There is incorrect data in the response from " +
                "Stats_service: expected stats for URI '%s', but was for '%s", uri, response.getBody()[0].getUri())));
    }

}