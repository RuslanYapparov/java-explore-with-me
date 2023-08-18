package ru.practicum.explore_with_me.main_service.mapper.impl;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.explore_with_me.main_service.mapper.ObjectMapper;
import ru.practicum.explore_with_me.main_service.model.db_entities.RequestEntity;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.request.Request;
import ru.practicum.explore_with_me.main_service.model.rest_dto.request.RequestRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.request.RequestRestView;

@Mapper(componentModel = "spring")
public interface RequestMapper extends ObjectMapper<RequestEntity, Request, RequestRestCommand, RequestRestView> {

    @Override
    @Mapping(target = "requester", ignore = true)
    @Mapping(target = "event", ignore = true)
    RequestEntity toDbEntity(Request request);

    @Override
    @Mapping(target = "requester", source = "requester.id")
    @Mapping(target = "event", source = "event.id")
    Request fromDbEntity(RequestEntity requestEntity);

}