package ru.practicum.explore_with_me.main_service.model.rest_dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import ru.practicum.explore_with_me.main_service.model.rest_dto.category.CategoryRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.user.UserShort;

import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
public class EventRestView {
    @JsonProperty("id")
    long id;
    @JsonProperty("title")
    String title;
    @JsonProperty("annotation")
    String annotation;
    @JsonProperty("description")
    String description;
    @JsonProperty("state")
    String state;
    @JsonProperty("eventDate")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;
    @JsonProperty("location")
    GeoLocation location;
    @JsonProperty("initiator")
    UserShort initiator;
    @JsonProperty("category")
    CategoryRestView category;
    @JsonProperty("participantLimit")
    int participantLimit;
    @JsonProperty("confirmedRequests")
    int confirmedRequests;
    @JsonProperty("paid")
    boolean paid;
    @JsonProperty("requestModeration")
    boolean requestModeration;
    @JsonProperty("createdOn")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdOn;
    @JsonProperty("publishedOn")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime publishedOn;
    @JsonProperty("views")
    long views;

}