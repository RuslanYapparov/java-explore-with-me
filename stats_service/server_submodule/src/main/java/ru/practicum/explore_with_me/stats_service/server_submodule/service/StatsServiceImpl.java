package ru.practicum.explore_with_me.stats_service.server_submodule.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.HitRestCommand;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.HitRestView;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.UriStatRestView;
import ru.practicum.explore_with_me.stats_service.server_submodule.dao.StatsRepository;
import ru.practicum.explore_with_me.stats_service.server_submodule.dao.UriStatFromDb;
import ru.practicum.explore_with_me.stats_service.server_submodule.mapper.StatElementsMapper;
import ru.practicum.explore_with_me.stats_service.server_submodule.dao.HitEntity;
import ru.practicum.explore_with_me.stats_service.server_submodule.util.MethodParameterValidator;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
@RequiredArgsConstructor
@Slf4j
public class StatsServiceImpl implements StatsService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final StatsRepository statsRepository;
    private final StatElementsMapper statElementsMapper;

    public HitRestView saveHit(@Valid HitRestCommand hitRestCommand) {
        MethodParameterValidator.validateRequestParameters(hitRestCommand.getIp(), hitRestCommand.getUri());
        HitEntity hitEntity = statElementsMapper.hitRestCommandToEntity(hitRestCommand);
        hitEntity = statsRepository.save(hitEntity);
        log.debug("New hit '{}' was saved", hitEntity);
        return statElementsMapper.hitEntityToRestView(hitEntity);
    }

    public List<UriStatRestView> getAllUriStatsOrderedByHits(
            @NotNull @NotBlank String start,
            @NotNull @NotBlank String end,
            String[] uris,
            Boolean ipUnique) {
        LocalDateTime startDateTime = start.equals("null") ?
                null : LocalDateTime.parse(URLDecoder.decode(start, StandardCharsets.UTF_8), FORMATTER);
        LocalDateTime endDateTime = end.equals("null") ?
                null : LocalDateTime.parse(URLDecoder.decode(end, StandardCharsets.UTF_8), FORMATTER);
        MethodParameterValidator.validateRequestParameters(startDateTime, endDateTime);
        List<UriStatFromDb> stats;
        if (ipUnique == null || !ipUnique) {
            stats = (uris == null || uris.length == 0) ?
                    statsRepository.getAllUriStatsOrderedByHitsCount(startDateTime, endDateTime) :
                    statsRepository.getAllUriStatsOrderedByHitsCountWithUrisArray(startDateTime, endDateTime, uris);
        } else {
            stats = (uris == null || uris.length == 0) ?
                    statsRepository.getUniqueUriStatsOrderedByHitsCount(startDateTime, endDateTime) :
                    statsRepository.getUniqueUriStatsOrderedByHitsCountWithUrisArray(startDateTime, endDateTime, uris);
        }
        log.debug("URI hit statistics was sent to client");
        return stats.stream()
                .map(statElementsMapper::uriStatFromDbToRestView)
                .collect(Collectors.toList());
    }

}