package ru.practicum.explore_with_me.main_service.util;

import lombok.experimental.UtilityClass;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;

import ru.practicum.explore_with_me.main_service.exception.StatsServiceProblemException;
import ru.practicum.explore_with_me.stats_service.client_submodule.StatsClient;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.EwmConstants;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.HitRestView;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.UriStatRestView;

import java.time.LocalDateTime;

@UtilityClass
public class StatsServiceIntegrator {
    private static final StatsClient statsClient = new StatsClient(new RestTemplateBuilder());

    public static void saveStatHitFromThisRequests(String ip, String uri) {
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

    public static UriStatRestView[] getUriStatsFromService(String[] uris) {
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

    public static long getViewsForOneUri(String uri) {
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

}
