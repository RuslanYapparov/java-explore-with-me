package ru.practicum.explore_with_me.main_service.mapper.impl;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.explore_with_me.main_service.model.db_entities.LikeEntity;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.Like;
import ru.practicum.explore_with_me.main_service.model.rest_dto.like.LikeRestCommand;

@Mapper(componentModel = "spring")
public interface LikeMapper {

    Like fromRestCommand(LikeRestCommand likeRestCommand);

    @Mapping(target = "user", source = "user.id")
    @Mapping(target = "event", source = "event.id")
    Like fromDbEntity(LikeEntity likeEntity);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "event", ignore = true)
    LikeEntity toDbEntity(Like like);

}