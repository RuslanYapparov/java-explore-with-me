package ru.practicum.explore_with_me.main_service.model.rest_dto.event;

import lombok.Value;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Value
public class HttpAdminGetAllRequestParamsHolder {
    long[] users;
    String[] states;
    long[] categories;
    String rangeStart;
    String rangeEnd;
    @PositiveOrZero
    int from;
    @Positive
    int size;

}