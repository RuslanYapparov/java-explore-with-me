package ru.practicum.explore_with_me.stats_service.server_submodule.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.HitRestCommand;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.HitRestView;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.UriStatRestView;
import ru.practicum.explore_with_me.stats_service.server_submodule.dao.UriStatFromDb;
import ru.practicum.explore_with_me.stats_service.server_submodule.dao.HitEntity;

import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public interface StatElementsMapper {
    DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Mapping(target = "timestamp",
            expression = "java(java.time.LocalDateTime.parse(hitRestCommand.getTimestamp(), FORMATTER))")
    HitEntity hitRestCommandToEntity(HitRestCommand hitRestCommand);

    HitRestView hitEntityToRestView(HitEntity hitEntity);

    UriStatRestView uriStatFromDbToRestView(UriStatFromDb uriStatFromDb);

}