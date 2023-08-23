package ru.practicum.explore_with_me.main_service.model.rest_dto.like;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Value
@Builder
public class HttpGetAuthorsWithRatingRequestParamsHolder {
    long[] categories;
    Boolean eventsAreOver;
    Boolean paid;
    boolean asc;
    @PositiveOrZero
    int from;
    @Positive
    int size;

}