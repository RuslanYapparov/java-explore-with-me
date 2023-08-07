package ru.practicum.explore_with_me.stats_service.server_submodule;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ru.practicum.explore_with_me.stats_service.client_submodule.StatsClient;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.HitRestView;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.UriStatRestView;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class StatsClientIntegrationTest {
    private final StatsClient statsClient = new StatsClient(new RestTemplateBuilder());
    private static final String DEFAULT_IP = "121.0.0.1";

    @Test
    public void addNewHit_whenGetCorrectParameters_thenReturnCorrectResponseEntity() {
        ResponseEntity<HitRestView> hitResponse = statsClient.addNewHit(DEFAULT_IP, "/events");

        assertThat(hitResponse.getStatusCode(), equalTo(HttpStatus.CREATED));
        assertThat(hitResponse.getBody(), notNullValue());
        assertThat(hitResponse.getBody().getId(), not(0));
        assertThat(hitResponse.getBody().getApplication(), equalTo("ewm-main-service"));
        assertThat(hitResponse.getBody().getIp(), equalTo(DEFAULT_IP));
        assertThat(hitResponse.getBody().getTimestamp(), notNullValue());
        assertTrue(hitResponse.getBody().getTimestamp().isBefore(LocalDateTime.now()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\n", "\r", "\t", "123.456.789.012", "potato", "125h.55r0.00t0.a345"})
    @NullSource
    public void addNewHit_whenGetIncorrectIpParameter_thenThrowException(String ip) {
        ResponseEntity<HitRestView> wrongHitResponse = statsClient.addNewHit(ip, "/events");
        assertThat(wrongHitResponse.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
        HitRestView wrongHit = wrongHitResponse.getBody();
        assertThat(wrongHit, notNullValue());
        if (ip == null) {
            assertThat(wrongHit.getApplication(), containsString("saveHit.hitRestCommand.ip: "));
        } else if (ip.isBlank()) {
            assertThat(wrongHit.getApplication(), oneOf("saveHit.hitRestCommand.ip: не должно быть пустым",
                    "saveHit.hitRestCommand.ip: must not be blank"));
        } else {
            assertThat(wrongHit.getApplication(), equalTo("Wrong method parameter: IP not in IPv4 or IPv6 format"));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\n", "\r", "\t", "potato", "/test"})
    @NullSource
    public void addNewHit_whenGetIncorrectUriParameter_thenThrowException(String uri) {
        ResponseEntity<HitRestView> wrongHitResponse = statsClient.addNewHit(DEFAULT_IP, uri);
        assertThat(wrongHitResponse.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
        HitRestView wrongHit = wrongHitResponse.getBody();
        assertThat(wrongHit, notNullValue());
        if (uri == null) {
            assertThat(wrongHit.getApplication(), containsString("saveHit.hitRestCommand.uri: "));
        } else if (uri.isBlank()) {
            assertThat(wrongHit.getApplication(), oneOf("saveHit.hitRestCommand.uri: не должно быть пустым",
                    "saveHit.hitRestCommand.uri: must not be blank"));
        } else {
            assertThat(wrongHit.getApplication(), equalTo("Wrong method parameter: URI not supported for saving hit"));
        }
    }

    @Test
    public void getUriStats_whenGetCorrectParameters_thenReturnCorrectResponseEntity() {
        statsClient.addNewHit(DEFAULT_IP, "/events");
        statsClient.addNewHit(DEFAULT_IP, "/events");
        statsClient.addNewHit(DEFAULT_IP, "/events");
        statsClient.addNewHit(DEFAULT_IP, "/events/777");
        statsClient.addNewHit(DEFAULT_IP, "/events/777");

        ResponseEntity<UriStatRestView[]> uriArrayResponse = statsClient.getUriStats(
                LocalDateTime.now().minusSeconds(1),
                LocalDateTime.now(),
                null,
                false);

        assertThat(uriArrayResponse.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(uriArrayResponse.getBody(), notNullValue());
        assertThat(uriArrayResponse.getBody(), arrayWithSize(2));
        UriStatRestView firstUriStats = uriArrayResponse.getBody()[0];
        assertThat(firstUriStats.getApplication(), equalTo("ewm-main-service"));
        assertThat(firstUriStats.getUri(), equalTo("/events"));
        assertTrue(firstUriStats.getHits() >= 3);
        UriStatRestView secondUriStats = uriArrayResponse.getBody()[1];
        assertThat(secondUriStats.getApplication(), equalTo("ewm-main-service"));
        assertThat(secondUriStats.getUri(), equalTo("/events/777"));
        assertEquals(2, secondUriStats.getHits());

        uriArrayResponse = statsClient.getUriStats(
                LocalDateTime.now().minusSeconds(1),
                LocalDateTime.now(),
                new String[] {"/events/777"},
                false);

        assertThat(uriArrayResponse.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(uriArrayResponse.getBody(), notNullValue());
        assertThat(uriArrayResponse.getBody(), arrayWithSize(1));
        firstUriStats = uriArrayResponse.getBody()[0];
        assertThat(firstUriStats.getApplication(), equalTo("ewm-main-service"));
        assertThat(firstUriStats.getUri(), equalTo("/events/777"));
        assertEquals(2, firstUriStats.getHits());

        uriArrayResponse = statsClient.getUriStats(
                LocalDateTime.now().minusSeconds(1),
                LocalDateTime.now(),
                null,
                true);

        assertThat(uriArrayResponse.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(uriArrayResponse.getBody(), notNullValue());
        assertThat(uriArrayResponse.getBody(), arrayWithSize(2));
        firstUriStats = uriArrayResponse.getBody()[0];
        assertThat(firstUriStats.getApplication(), equalTo("ewm-main-service"));
        assertThat(firstUriStats.getUri(), equalTo("/events"));
        assertEquals(1, firstUriStats.getHits());
        secondUriStats = uriArrayResponse.getBody()[1];
        assertThat(secondUriStats.getApplication(), equalTo("ewm-main-service"));
        assertThat(secondUriStats.getUri(), equalTo("/events/777"));
        assertEquals(1, secondUriStats.getHits());

        uriArrayResponse = statsClient.getUriStats(
                LocalDateTime.now().minusSeconds(1),
                LocalDateTime.now(),
                new String[] {"/events"},
                true);

        assertThat(uriArrayResponse.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(uriArrayResponse.getBody(), notNullValue());
        assertThat(uriArrayResponse.getBody(), arrayWithSize(1));
        firstUriStats = uriArrayResponse.getBody()[0];
        assertThat(firstUriStats.getApplication(), equalTo("ewm-main-service"));
        assertThat(firstUriStats.getUri(), equalTo("/events"));
        assertEquals(1, firstUriStats.getHits());

        statsClient.addNewHit("121.0.0.2", "/events/777");
        uriArrayResponse = statsClient.getUriStats(
                LocalDateTime.now().minusSeconds(1),
                LocalDateTime.now(),
                null,
                false);

        assertThat(uriArrayResponse.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(uriArrayResponse.getBody(), notNullValue());
        assertThat(uriArrayResponse.getBody(), arrayWithSize(2));
        firstUriStats = uriArrayResponse.getBody()[0];
        assertThat(firstUriStats.getApplication(), equalTo("ewm-main-service"));
        assertThat(firstUriStats.getUri(), equalTo("/events"));
        assertTrue(firstUriStats.getHits() >= 3);
        secondUriStats = uriArrayResponse.getBody()[1];
        assertThat(secondUriStats.getApplication(), equalTo("ewm-main-service"));
        assertThat(secondUriStats.getUri(), equalTo("/events/777"));
        assertEquals(3, secondUriStats.getHits());

        uriArrayResponse = statsClient.getUriStats(
                LocalDateTime.now().minusSeconds(1),
                LocalDateTime.now(),
                new String[] {"/events/777"},
                false);

        assertThat(uriArrayResponse.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(uriArrayResponse.getBody(), notNullValue());
        assertThat(uriArrayResponse.getBody(), arrayWithSize(1));
        firstUriStats = uriArrayResponse.getBody()[0];
        assertThat(firstUriStats.getApplication(), equalTo("ewm-main-service"));
        assertThat(firstUriStats.getUri(), equalTo("/events/777"));
        assertEquals(3, firstUriStats.getHits());

        uriArrayResponse = statsClient.getUriStats(
                LocalDateTime.now().minusSeconds(1),
                LocalDateTime.now(),
                null,
                true);

        assertThat(uriArrayResponse.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(uriArrayResponse.getBody(), notNullValue());
        assertThat(uriArrayResponse.getBody(), arrayWithSize(2));
        firstUriStats = uriArrayResponse.getBody()[0];
        assertThat(firstUriStats.getApplication(), equalTo("ewm-main-service"));
        assertThat(firstUriStats.getUri(), equalTo("/events/777"));
        assertEquals(2, firstUriStats.getHits());
        secondUriStats = uriArrayResponse.getBody()[1];
        assertThat(secondUriStats.getApplication(), equalTo("ewm-main-service"));
        assertThat(secondUriStats.getUri(), equalTo("/events"));
        assertEquals(1, secondUriStats.getHits());

        uriArrayResponse = statsClient.getUriStats(
                LocalDateTime.now().minusSeconds(1),
                LocalDateTime.now(),
                new String[] {"/events/777"},
                true);

        assertThat(uriArrayResponse.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(uriArrayResponse.getBody(), notNullValue());
        assertThat(uriArrayResponse.getBody(), arrayWithSize(1));
        firstUriStats = uriArrayResponse.getBody()[0];
        assertThat(firstUriStats.getApplication(), equalTo("ewm-main-service"));
        assertThat(firstUriStats.getUri(), equalTo("/events/777"));
        assertEquals(2, firstUriStats.getHits());

        statsClient.addNewHit("121.0.0.3", "/events/1");
        uriArrayResponse = statsClient.getUriStats(
                LocalDateTime.now().minusSeconds(1),
                LocalDateTime.now(),
                null,
                false);

        assertThat(uriArrayResponse.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(uriArrayResponse.getBody(), notNullValue());
        assertThat(uriArrayResponse.getBody(), arrayWithSize(3));
        firstUriStats = uriArrayResponse.getBody()[0];
        assertThat(firstUriStats.getApplication(), equalTo("ewm-main-service"));
        assertThat(firstUriStats.getUri(), equalTo("/events"));
        assertTrue(firstUriStats.getHits() >= 3);
        secondUriStats = uriArrayResponse.getBody()[1];
        assertThat(secondUriStats.getApplication(), equalTo("ewm-main-service"));
        assertThat(secondUriStats.getUri(), equalTo("/events/777"));
        assertEquals(3, secondUriStats.getHits());
        UriStatRestView thirdUriStats = uriArrayResponse.getBody()[2];
        assertThat(thirdUriStats.getApplication(), equalTo("ewm-main-service"));
        assertThat(thirdUriStats.getUri(), equalTo("/events/1"));
        assertEquals(1, thirdUriStats.getHits());

        uriArrayResponse = statsClient.getUriStats(
                LocalDateTime.now().minusSeconds(1),
                LocalDateTime.now(),
                new String[] {"/events/777"},
                false);

        assertThat(uriArrayResponse.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(uriArrayResponse.getBody(), notNullValue());
        assertThat(uriArrayResponse.getBody(), arrayWithSize(1));
        firstUriStats = uriArrayResponse.getBody()[0];
        assertThat(firstUriStats.getApplication(), equalTo("ewm-main-service"));
        assertThat(firstUriStats.getUri(), equalTo("/events/777"));
        assertEquals(3, firstUriStats.getHits());

        uriArrayResponse = statsClient.getUriStats(
                LocalDateTime.now().minusSeconds(1),
                LocalDateTime.now(),
                null,
                true);

        assertThat(uriArrayResponse.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(uriArrayResponse.getBody(), notNullValue());
        assertThat(uriArrayResponse.getBody(), arrayWithSize(3));
        firstUriStats = uriArrayResponse.getBody()[0];
        assertThat(firstUriStats.getApplication(), equalTo("ewm-main-service"));
        assertThat(firstUriStats.getUri(), equalTo("/events/777"));
        assertEquals(2, firstUriStats.getHits());
        secondUriStats = uriArrayResponse.getBody()[1];
        assertThat(secondUriStats.getApplication(), equalTo("ewm-main-service"));
        assertThat(secondUriStats.getUri(), oneOf("/events", "/events/1"));
        assertEquals(1, secondUriStats.getHits());

        uriArrayResponse = statsClient.getUriStats(
                LocalDateTime.now().minusSeconds(1),
                LocalDateTime.now(),
                new String[] {"/events/777"},
                true);

        assertThat(uriArrayResponse.getStatusCode(), equalTo(HttpStatus.OK));
        assertThat(uriArrayResponse.getBody(), notNullValue());
        assertThat(uriArrayResponse.getBody(), arrayWithSize(1));
        firstUriStats = uriArrayResponse.getBody()[0];
        assertThat(firstUriStats.getApplication(), equalTo("ewm-main-service"));
        assertThat(firstUriStats.getUri(), equalTo("/events/777"));
        assertEquals(2, firstUriStats.getHits());
    }

    @Test
    public void getUriStats_whenGetIncorrectParameters_thenReturnCorrectResponseEntity() {
        ResponseEntity<UriStatRestView[]> wrongUriStatResponse = statsClient.getUriStats(
                null,
                LocalDateTime.now(),
                null,
                false);
        assertThat(wrongUriStatResponse.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
        UriStatRestView[] wrongUris = wrongUriStatResponse.getBody();
        assertThat(wrongUris, notNullValue());
        assertThat(wrongUris[0].getApplication(), equalTo("Wrong method parameter: " +
                "start or end of period for request cannot be null"));

        wrongUriStatResponse = statsClient.getUriStats(
                        LocalDateTime.now().minusSeconds(1),
                        null,
                        null,
                        false);
        assertThat(wrongUriStatResponse.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
        wrongUris = wrongUriStatResponse.getBody();
        assertThat(wrongUris, notNullValue());
        assertThat(wrongUris[0].getApplication(), equalTo("Wrong method parameter: " +
                "start or end of period for request cannot be null"));

        wrongUriStatResponse = statsClient.getUriStats(
                        LocalDateTime.now().plusSeconds(1),
                        LocalDateTime.now().plusSeconds(2),
                        null,
                        false);
        assertThat(wrongUriStatResponse.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
        wrongUris = wrongUriStatResponse.getBody();
        assertThat(wrongUris, notNullValue());
        assertThat(wrongUris[0].getApplication(), equalTo("Wrong method parameter: " +
                "start of period for requesting cannot be after current moment"));

        wrongUriStatResponse = statsClient.getUriStats(
                        LocalDateTime.now().minusSeconds(1),
                        LocalDateTime.now().minusSeconds(2),
                        null,
                        false);
        assertThat(wrongUriStatResponse.getStatusCode(), equalTo(HttpStatus.BAD_REQUEST));
        wrongUris = wrongUriStatResponse.getBody();
        assertThat(wrongUris, notNullValue());
        assertThat(wrongUris[0].getApplication(), equalTo("Wrong method parameter: " +
                "start of period for requesting cannot be after its end"));
    }

}