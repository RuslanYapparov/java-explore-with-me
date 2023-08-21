package ru.practicum.explore_with_me.main_service.mapper.impl;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import org.mapstruct.Named;
import ru.practicum.explore_with_me.main_service.mapper.ObjectMapper;
import ru.practicum.explore_with_me.main_service.model.db_entities.event.EventEntity;
import ru.practicum.explore_with_me.main_service.model.db_entities.event.GeoLocationEntity;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.event.Event;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.EventRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.EventRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.EventRestViewShort;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.GeoLocation;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.EwmConstants;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface EventMapper extends ObjectMapper<EventEntity, Event, EventRestCommand, EventRestView> {

    @Override
    @Mapping(target = "category.id", source = "category")
    @Mapping(target = "eventDate", qualifiedByName = "mapEventDate")
    Event fromRestCommand(EventRestCommand eventRestCommand);

    EventRestViewShort mapEventRestViewShortFromEvent(Event event);

    GeoLocationEntity mapGeoLocationToGeoLocationEntity(GeoLocation geoLocation);

    @Named("mapEventDate")
    default LocalDateTime mapEventDateFromString(String eventDate) {
        return LocalDateTime.parse(eventDate, EwmConstants.FORMATTER);
    }

}