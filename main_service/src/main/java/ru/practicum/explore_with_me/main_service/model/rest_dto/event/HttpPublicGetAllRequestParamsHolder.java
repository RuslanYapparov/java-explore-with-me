package ru.practicum.explore_with_me.main_service.model.rest_dto.event;

import lombok.Builder;
import lombok.Value;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Value
@Builder
public class HttpPublicGetAllRequestParamsHolder {
    String text;
    long[] categories;
    Boolean paid;
    String rangeStart;
    String rangeEnd;
    boolean onlyAvailable;
    @NotNull
    @NotBlank
    String sort;
    @PositiveOrZero
    int from;
    @Positive
    int size;

}