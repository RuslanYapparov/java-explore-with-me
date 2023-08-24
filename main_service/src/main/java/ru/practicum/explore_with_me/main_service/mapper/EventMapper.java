package ru.practicum.explore_with_me.main_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import ru.practicum.explore_with_me.main_service.model.db_entities.LikeEntity;
import ru.practicum.explore_with_me.main_service.model.db_entities.event.EventEntity;
import ru.practicum.explore_with_me.main_service.model.db_entities.event.GeoLocationEntity;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.event.Event;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.EventRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.EventRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.EventRestViewShort;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.GeoLocation;
import ru.practicum.explore_with_me.main_service.model.rest_dto.like.LikedEventsRestView;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.EwmConstants;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "category.id", source = "category")
    @Mapping(target = "eventDate", qualifiedByName = "mapEventDate")
    Event fromRestCommand(EventRestCommand eventRestCommand);

    EventRestView toRestView(Event event);

    Event fromDbEntity(EventEntity eventEntity);

    EventEntity toDbEntity(Event event);

    EventRestViewShort mapEventRestViewShortFromEvent(Event event);

    GeoLocationEntity mapGeoLocationToGeoLocationEntity(GeoLocation geoLocation);

    @Named("mapEventDate")
    default LocalDateTime mapEventDateFromString(String eventDate) {
        return LocalDateTime.parse(eventDate, EwmConstants.FORMATTER);
    }

    default LikedEventsRestView mapLikedEventsRestViewFromListOfEvents(List<LikeEntity> likeEntities) {
        LikedEventsRestView likedEventsRestView = LikedEventsRestView.builder()
                .liked(new TreeSet<>(Comparator.comparingLong(EventRestViewShort::getId)))
                .disliked(new TreeSet<>(Comparator.comparingLong(EventRestViewShort::getId)))
                .build();

        likeEntities.forEach(likeEntity -> {
            if (likeEntity.isLike()) {
                likedEventsRestView.getLiked()
                        .add(mapEventRestViewShortFromEvent(fromDbEntity(likeEntity.getEvent())));
            } else {
                likedEventsRestView.getDisliked()
                        .add(mapEventRestViewShortFromEvent(fromDbEntity(likeEntity.getEvent())));
            }
        });
        return likedEventsRestView;
    }

}