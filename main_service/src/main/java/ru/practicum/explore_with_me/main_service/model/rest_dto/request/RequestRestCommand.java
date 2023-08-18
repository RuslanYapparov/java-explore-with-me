package ru.practicum.explore_with_me.main_service.model.rest_dto.request;

import lombok.Builder;
import lombok.Value;
import javax.validation.constraints.Positive;

@Value
@Builder
public class RequestRestCommand {
    @Positive
    long requester;
    @Positive
    long event;

}