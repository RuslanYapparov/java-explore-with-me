package ru.practicum.explore_with_me.main_service.model.rest_dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
public class RequestRestView {
    @JsonProperty("id")
    BigInteger id;
    @JsonProperty("requester")
    long requester;
    @JsonProperty("event")
    long event;
    @JsonProperty("created")
    LocalDateTime createdOn;
    @JsonProperty("status")
    String status;

}