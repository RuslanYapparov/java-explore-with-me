package ru.practicum.explore_with_me.stats_service.server_submodule.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class UriStatFromDb {
    private String application;
    private String uri;
    private long hits;

}
