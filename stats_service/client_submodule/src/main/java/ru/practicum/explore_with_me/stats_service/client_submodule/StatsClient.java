package ru.practicum.explore_with_me.stats_service.client_submodule;

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
        HttpEntity<HitRestCommand> requestHitEntity = new HttpEntity<>(HitRestCommand.builder()
                .application("ewm-main-service")
                .uri(uri)
                .ip(ip)
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build());
        try {
            return restTemplate.exchange(
                    "/hit",
                    HttpMethod.POST,
                    requestHitEntity,
                    HitRestView.class);
        } catch (HttpStatusCodeException exception) {
            return ResponseEntity.badRequest().body(HitRestView.builder()
                    .application(exception.getResponseBodyAsString())
                    .build());
        }
    }

    public ResponseEntity<UriStatRestView[]> getUriStats(
            LocalDateTime start, LocalDateTime end, String[] uris, boolean unique) {
        Map<String, Object> parameters = Map.of(
                "start", start != null ? URLEncoder.encode(start.format(FORMATTER), StandardCharsets.UTF_8) : "null",
                "end", end != null ? URLEncoder.encode(end.format(FORMATTER), StandardCharsets.UTF_8) : "null",
                "uris", uris == null ? new String[] {} : uris,
                "unique", unique
        );
        try {
            return restTemplate.exchange(
                    "/stats?start={start}&end={end}&uris={uris}&unique={unique}",
                    HttpMethod.GET,
                    null,
                    UriStatRestView[].class,
                    parameters);
        } catch (HttpStatusCodeException exception) {
            return ResponseEntity.badRequest().body(new UriStatRestView[] {UriStatRestView.builder()
                    .application(exception.getResponseBodyAsString())
                    .build()});
        }
    }

    public void changRestTemplate(String host) {
        this.restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory("http://" + host + ":9090"));
    }

}