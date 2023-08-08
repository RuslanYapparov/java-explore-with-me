package ru.practicum.explore_with_me.stats_service.server_submodule.service;

import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.HitRestCommand;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.HitRestView;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.UriStatRestView;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

public interface StatsService {

    HitRestView saveHit(@Valid HitRestCommand hitRestCommand);

    List<UriStatRestView> getAllUriStatsOrderedByHits(
            @NotNull @NotBlank String start,
            @NotNull @NotBlank String end,
            String[] uris,
            Boolean ipUnique);

}