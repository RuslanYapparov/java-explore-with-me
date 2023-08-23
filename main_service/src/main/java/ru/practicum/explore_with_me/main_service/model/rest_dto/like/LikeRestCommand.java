package ru.practicum.explore_with_me.main_service.model.rest_dto.like;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.Positive;

@Value
@Builder(toBuilder = true)
public class LikeRestCommand {
    @Positive
    long user;
    @Positive
    long event;
    boolean isLike;

}