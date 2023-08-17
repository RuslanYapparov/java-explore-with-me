package ru.practicum.explore_with_me.main_service.model.domain_pojo.params_holder;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class JpaPublicGetAllQueryParamsHolder {
    String text;
    long[] categories;
    Boolean paid;
    LocalDateTime rangeStart;
    LocalDateTime rangeEnd;
    boolean onlyAvailable;
    SortBy sort;
    int from;
    int size;

}