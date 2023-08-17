package ru.practicum.explore_with_me.main_service.model.rest_dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.*;

@Value
@Builder(toBuilder = true)
public class EventRestCommand {
    @Positive
    @JsonProperty("category")
    long category;
    @NotNull
    @NotBlank
    @Size(min = 3, max = 120)
    @JsonProperty("title")
    String title;
    @NotNull
    @NotBlank
    @Size(min = 20, max = 2000)
    @JsonProperty("annotation")
    String annotation;
    @NotNull
    @NotBlank
    @Size(min = 20, max = 7000)
    @JsonProperty("description")
    String description;
    @NotNull
    @JsonProperty("eventDate")
    String eventDate;
    @JsonProperty("location")
    GeoLocation location;
    @JsonProperty("paid")
    Boolean paid;
    @JsonProperty("requestModeration")
    Boolean requestModeration;
    @JsonProperty("participantLimit")
    Integer participantLimit;
    @JsonProperty("stateAction")
    String stateAction;

}