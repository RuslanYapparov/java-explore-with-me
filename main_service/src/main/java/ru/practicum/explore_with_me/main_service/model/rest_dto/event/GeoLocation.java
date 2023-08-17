package ru.practicum.explore_with_me.main_service.model.rest_dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GeoLocation {
    @JsonProperty("lat")
    double latitude;
    @JsonProperty("lon")
    double longitude;

}