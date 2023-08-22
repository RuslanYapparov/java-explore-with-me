package ru.practicum.explore_with_me.main_service.model.rest_dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigInteger;

@Value
@Builder
public class RequestStatusSetRestCommand {
    @NotNull
    @JsonProperty("requestIds")
    BigInteger[] requestIds;
    @NotNull
    @NotBlank
    @JsonProperty("status")
    String status;

}