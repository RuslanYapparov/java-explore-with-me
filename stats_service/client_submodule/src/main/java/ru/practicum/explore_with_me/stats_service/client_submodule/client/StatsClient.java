package ru.practicum.explore_with_me.stats_service.client_submodule.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.apache.commons.validator.routines.InetAddressValidator;

import ru.practicum.explore_with_me.stats_service.client_submodule.exception.BadRequestParameterException;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.HitRestCommand;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.HitRestView;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.UriStatRestView;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.net.URLEncoder;

@Service
public class StatsClient {
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final RestTemplate restTemplate;

    @Autowired
    public StatsClient(RestTemplateBuilder builder) {
        this.restTemplate = builder.uriTemplateHandler(new DefaultUriBuilderFactory("http://localhost:9090"))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build();
    }

    public ResponseEntity<HitRestView> addNewHit(String ip, String uri) {
        validateRequestParameters(ip, uri);
        HttpEntity<HitRestCommand> requestHitEntity = new HttpEntity<>(HitRestCommand.builder()
                .application("ewm-main-service")
                .uri(uri)
                .ip(ip)
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build());
        ResponseEntity<HitRestView> serverSubmoduleHitResponse;
        try {
            serverSubmoduleHitResponse = restTemplate.exchange(
                    "/hit",
                    HttpMethod.POST,
                    requestHitEntity,
                    HitRestView.class);
        } catch (HttpStatusCodeException exception) {
            return ResponseEntity.badRequest().body(HitRestView.builder().build());
        }
        if (serverSubmoduleHitResponse.getStatusCode().is2xxSuccessful()) {
            return serverSubmoduleHitResponse;
        } else {
            return ResponseEntity.badRequest().body(HitRestView.builder().build());
        }
    }

    public ResponseEntity<UriStatRestView[]> getUriStats(
            LocalDateTime start, LocalDateTime end, String[] uris, boolean unique) {
        validateRequestParameters(start, end);
        Map<String, Object> parameters = Map.of(
                "start", URLEncoder.encode(start.format(FORMATTER), StandardCharsets.UTF_8),
                "end", URLEncoder.encode(end.format(FORMATTER), StandardCharsets.UTF_8),
                "uris", uris == null ? new String[] {} : uris,
                "unique", unique
        );
        ResponseEntity<UriStatRestView[]> serverSubmoduleListOfUriStatsResponse;
        try {
            serverSubmoduleListOfUriStatsResponse = restTemplate.exchange(
                    "/stats?start={start}&end={end}&uris={uris}&unique={unique}",
                    HttpMethod.GET,
                    null,
                    UriStatRestView[].class,
                    parameters);
        } catch (HttpStatusCodeException exception) {
            return ResponseEntity.badRequest().body(new UriStatRestView[] {});
        }
        if (serverSubmoduleListOfUriStatsResponse.getStatusCode().is2xxSuccessful()) {
            return serverSubmoduleListOfUriStatsResponse;
        } else {
            return ResponseEntity.badRequest().body(new UriStatRestView[] {});
        }
    }

    private void validateRequestParameters(String ip, String uri) {
        if (ip == null || uri == null) {
            throw new BadRequestParameterException("Wrong method parameter: IP or URI cannot be null");
        }
        if (ip.isBlank() || uri.isBlank()) {
            throw new BadRequestParameterException("Wrong method parameter: IP or URI cannot be empty values");
        }
        if (!InetAddressValidator.getInstance().isValid(ip)) {
            throw new BadRequestParameterException("Wrong method parameter: IP not in IPv4 or IPv6 format");
        }
        if (!uri.startsWith("/events")) {
            throw new BadRequestParameterException("Wrong method parameter: URI not supported for saving hit");
        }
    }

    private void validateRequestParameters(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new BadRequestParameterException("Wrong method parameter: " +
                    "start or end of period for request cannot be null");
        }
        if (start.isAfter(end)) {
            throw new BadRequestParameterException("Wrong method parameter: " +
                    "start of period for requesting cannot be after its end");
        }
        if (start.isAfter(LocalDateTime.now())) {
            throw new BadRequestParameterException("Wrong method parameter: " +
                    "start of period for requesting cannot be after current moment");
        }
    }

}