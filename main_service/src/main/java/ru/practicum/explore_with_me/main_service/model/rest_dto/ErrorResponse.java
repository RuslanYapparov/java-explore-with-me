package ru.practicum.explore_with_me.main_service.model.rest_dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
public class ErrorResponse {
    @JsonProperty("status")
    HttpStatus status;
    @JsonProperty("reason")
    String reason;
    @JsonProperty("message")
    String message;
    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime timestamp;

}