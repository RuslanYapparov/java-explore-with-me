package ru.practicum.explore_with_me.main_service.util;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Component;
import ru.practicum.explore_with_me.main_service.exception.StatsServiceProblemException;
import ru.practicum.explore_with_me.main_service.mapper.impl.EventMapper;
import ru.practicum.explore_with_me.main_service.model.db_entities.event.EventEntity;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.event.Event;
import ru.practicum.explore_with_me.stats_service.client_submodule.StatsClient;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.EwmConstants;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.HitRestView;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.UriStatRestView;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StatsServiceIntegrator {
    private final StatsClient statsClient;
    private final EventMapper eventMapper;

    public void saveStatHitFromThisRequests(String ip, String uri) {
        ResponseEntity<HitRestView> statsServiceResponse = statsClient.addNewHit(ip, uri);
        HitRestView hitUri = statsServiceResponse.getBody();
        if (hitUri == null) {
            throw new StatsServiceProblemException("Failed to get data from Stats_sever: there is null hit body " +
                    "in the response from server");
        }
        if (statsServiceResponse.getStatusCode().is4xxClientError()) {
            throw new StatsServiceProblemException(hitUri.getApplication());
        }  // Клиент записывает информацию об ошибке в поле application объекта HitRestView, если происходит ошибка
    }

    public UriStatRestView[] getUriStatsFromService(String[] uris) {
        ResponseEntity<UriStatRestView[]> statsServerResponse = statsClient.getUriStats(
                EwmConstants.DEFAULT_DATE_TIME,
                LocalDateTime.now(),
                uris,
                true);
        UriStatRestView[] uriStats = statsServerResponse.getBody();
        if (uriStats == null) {
            throw new StatsServiceProblemException("Failed to get data from Stats_sever: there is null statistics " +
                    "array in the response from server");
        }
        if (statsServerResponse.getStatusCode().is4xxClientError()) {
            throw new StatsServiceProblemException(uriStats[0].getApplication());
        }  // Клиент записывает информацию об ошибке в поле application объекта UriStatRestView, если происходит ошибка
        return uriStats;
    }

    public long getViewsForOneUri(String uri) {
        UriStatRestView[] uriStats = getUriStatsFromService(new String[] {uri});
        if (uriStats.length == 0) {
            return 0L;
        } else if (!uriStats[0].getUri().equals(uri)) {
            throw new StatsServiceProblemException(String.format("There is incorrect data in the response from " +
                    "Stats_service: expected stats for URI '%s', but was for '%s", uri, uriStats[0].getUri()));
        } else {
            return uriStats[0].getHits();
        }
    }

    public List<Event> mapEventEntitiesToEventsWithViews(Collection<EventEntity> entities) {
        String[] uris = entities.stream()
                .map(EventEntity::getId)
                .map(entityId -> "/events/" + entityId)
                .collect(Collectors.toList()).toArray(new String[] {});
        Map<Long, Long> viewsStatistics = new HashMap<>();
        Arrays.stream(getUriStatsFromService(uris))
                .filter(uriStat -> !(uriStat.getUri().equals("/events")))
                .forEach(uriStat -> {
                    long eventId;
                    try {
                        eventId = Long.parseLong(uriStat.getUri().substring(8));
                    } catch (NumberFormatException exception) {
                        throw new StatsServiceProblemException("Unsupported URI format found in response from " +
                                "Stats_service: " + uriStat.getUri());
                    }
                    viewsStatistics.put(eventId, uriStat.getHits());
                });
        return entities.stream()
                .map(eventEntity -> eventMapper.fromDbEntity(eventEntity).toBuilder()
                        .views(viewsStatistics.getOrDefault(eventEntity.getId(), 0L))
                        .build())
                .collect(Collectors.toList());
    }

}
