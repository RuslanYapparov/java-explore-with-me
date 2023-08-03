package ru.practicum.explore_with_me.stats_service.server_submodule.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.HitRestCommand;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.HitRestView;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.UriStatRestView;
import ru.practicum.explore_with_me.stats_service.server_submodule.service.StatsService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public HitRestView saveNewHit(@RequestBody HitRestCommand hitRestCommand) {
        return statsService.saveHit(hitRestCommand);
    }

    @GetMapping("/stats")
    public List<UriStatRestView> getUriStats(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(required = false) String[] uris,
            @RequestParam(required = false) Boolean unique) {
        return statsService.getAllUriStatsOrderedByHits(start, end, uris, unique);
    }

}