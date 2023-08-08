package ru.practicum.explore_with_me.stats_service.dto_submodule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Value;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = HitRestView.HitRestViewBuilder.class)
public class HitRestView {
    @JsonProperty("id")
    BigInteger id;
    @JsonProperty("app")
    String application;
    @JsonProperty("uri")
    String uri;
    @JsonProperty("ip")
    String ip;
    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime timestamp;

}