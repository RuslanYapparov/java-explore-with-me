package ru.practicum.explore_with_me.main_service.model.domain_pojo.event;

import lombok.Builder;
import lombok.Value;

import ru.practicum.explore_with_me.main_service.model.domain_pojo.Category;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.GeoLocation;
import ru.practicum.explore_with_me.main_service.model.rest_dto.user.UserShort;

import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
public class Event {
    long id;
    String title;
    String description;
    String annotation;
    LocalDateTime eventDate;
    GeoLocation location;
    UserShort initiator;
    Category category;
    boolean paid;
    boolean requestModeration;
    int participantLimit;
    int confirmedRequests;
    int rating;
    int numberOfLikes;
    EventState state;
    LocalDateTime createdOn;
    LocalDateTime publishedOn;
    long views;

}