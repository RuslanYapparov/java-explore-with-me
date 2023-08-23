package ru.practicum.explore_with_me.main_service.model.domain_pojo.params_holder;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Value
@Builder
public class JpaGetEventAuthorsQueryParamsHolder {
    long[] categories;
    Boolean eventsAreOver;
    Boolean paid;
    boolean ascendingDirection;
    @PositiveOrZero
    int from;
    @Positive
    int size;

}