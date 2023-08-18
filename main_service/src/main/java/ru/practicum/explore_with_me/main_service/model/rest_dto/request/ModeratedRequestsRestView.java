package ru.practicum.explore_with_me.main_service.model.rest_dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.util.Set;

@Value
@Builder(toBuilder = true)
public class ModeratedRequestsRestView {
    @JsonProperty("confirmedRequests")
    Set<RequestRestView> confirmed;
    @JsonProperty("rejectedRequests")
    Set<RequestRestView> rejected;

}