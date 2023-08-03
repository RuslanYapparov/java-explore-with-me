package ru.practicum.explore_with_me.stats_service.dto_submodule.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = UriStatRestView.UriStatRestViewBuilder.class)
public class UriStatRestView {
    @JsonProperty("app")
    String application;
    @JsonProperty("uri")
    String uri;
    @JsonProperty("hits")
    long hits;


}