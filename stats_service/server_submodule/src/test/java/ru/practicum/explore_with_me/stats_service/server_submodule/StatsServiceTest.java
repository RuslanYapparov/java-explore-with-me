package ru.practicum.explore_with_me.stats_service.server_submodule;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import ru.practicum.explore_with_me.stats_service.client_submodule.StatsClient;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.HitRestCommand;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.HitRestView;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.UriStatRestView;
import ru.practicum.explore_with_me.stats_service.server_submodule.service.StatsService;

import javax.validation.ConstraintViolationException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class StatsServiceTest {
    private final StatsService statsService;
    private static final LocalDateTime DEFAULT_DATE_TIME = LocalDateTime.of(2023,8,1, 0, 0, 1);

    @Test
    public void save_whenGetCorrectHitRestCommand_thenReturnHitRestView() {
        HitRestView savedHit = statsService.saveHit(HitRestCommand.builder()
                .application("application")
                .ip("127.0.0.1")
                .uri("/events")
                .timestamp(DEFAULT_DATE_TIME.format(StatsClient.FORMATTER))
                .build());
        assertTrue(savedHit.getId().intValue() >= 1);
        assertThat(savedHit.getApplication(), equalTo("application"));
        assertThat(savedHit.getIp(), equalTo("127.0.0.1"));
        assertThat(savedHit.getUri(), equalTo("/events"));
        assertThat(savedHit.getTimestamp(), equalTo(DEFAULT_DATE_TIME));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\n", "\t", "\r"})
    @NullSource
    public void save_whenGetIncorrectHitRestCommand_thenThrowException(String value) {
        assertThrows(ConstraintViolationException.class, () ->
                statsService.saveHit(HitRestCommand.builder().build()));

        assertThrows(ConstraintViolationException.class, () ->
                statsService.saveHit(HitRestCommand.builder()
                .application(value)
                .ip("127.0.0.1")
                .uri("/events")
                .timestamp(DEFAULT_DATE_TIME.format(StatsClient.FORMATTER))
                .build()));

        assertThrows(ConstraintViolationException.class, () ->
                statsService.saveHit(HitRestCommand.builder()
                        .application("application")
                        .ip(value)
                        .uri("/events")
                        .timestamp(DEFAULT_DATE_TIME.format(StatsClient.FORMATTER))
                        .build()));

        assertThrows(ConstraintViolationException.class, () ->
                statsService.saveHit(HitRestCommand.builder()
                        .application("application")
                        .ip("127.0.0.1")
                        .uri(value)
                        .timestamp(DEFAULT_DATE_TIME.format(StatsClient.FORMATTER))
                        .build()));
        assertThrows(ConstraintViolationException.class, () ->
                statsService.saveHit(HitRestCommand.builder()
                        .application("application")
                        .ip("127.0.0.1")
                        .uri("/events")
                        .timestamp(value)
                        .build()));
    }

    @Test
    public void getAllUriStatsOrderedByHits_whenGetCorrectParameters_thenReturnListOfUriStatsRestViews() {
        statsService.saveHit(HitRestCommand.builder()
                .application("application")
                .ip("127.0.0.1")
                .uri("/events/777")
                .timestamp(DEFAULT_DATE_TIME.format(StatsClient.FORMATTER))
                .build());
        statsService.saveHit(HitRestCommand.builder()
                .application("application")
                .ip("127.0.0.1")
                .uri("/events/777")
                .timestamp(DEFAULT_DATE_TIME.format(StatsClient.FORMATTER))
                .build());
        statsService.saveHit(HitRestCommand.builder()
                .application("application")
                .ip("127.0.0.1")
                .uri("/events")
                .timestamp(DEFAULT_DATE_TIME.format(StatsClient.FORMATTER))
                .build());

        List<UriStatRestView> uriStats = statsService.getAllUriStatsOrderedByHits(
                DEFAULT_DATE_TIME.minusSeconds(1).format(StatsClient.FORMATTER),
                LocalDateTime.now().format(StatsClient.FORMATTER),
                null,
                null
        );
        assertThat(uriStats, iterableWithSize(2));
        assertThat(uriStats.get(0), equalTo(UriStatRestView.builder()
                .application("application")
                .uri("/events/777")
                .hits(2)
                .build()));
        assertThat(uriStats.get(1), equalTo(UriStatRestView.builder()
                .application("application")
                .uri("/events")
                .hits(1)
                .build()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\n", "\t", "\r"})
    @NullSource
    public void getAllUriStatsOrderedByHits_whenGetIncorrectParameter_thenThrowException(String value) {
        assertThrows(ConstraintViolationException.class, () ->
                statsService.getAllUriStatsOrderedByHits(
                        value,
                        LocalDateTime.now().format(StatsClient.FORMATTER),
                        null,
                        null));
        assertThrows(ConstraintViolationException.class, () ->
                statsService.getAllUriStatsOrderedByHits(
                        LocalDateTime.now().format(StatsClient.FORMATTER),
                        value,
                        null,
                        null));
    }

}