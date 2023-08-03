package ru.practicum.explore_with_me.stats_service.dto_submodule.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Value
@Builder(toBuilder = true)
public class HitRestCommand {
    @JsonProperty("app")
    @NotNull
    @NotBlank
    String application;
    @JsonProperty("uri")
    @NotNull
    @NotBlank
    String uri;
    @JsonProperty("ip")
    @NotNull
    @NotBlank
    String ip;
    @JsonProperty("timestamp")
    @NotNull
    @NotBlank
    String timestamp;

}