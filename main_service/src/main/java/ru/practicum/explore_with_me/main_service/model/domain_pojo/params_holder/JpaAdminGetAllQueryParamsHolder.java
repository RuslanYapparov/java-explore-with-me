package ru.practicum.explore_with_me.main_service.model.domain_pojo.params_holder;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class JpaAdminGetAllQueryParamsHolder {
    long[] users;
    String[] states;
    long[] categories;
    LocalDateTime rangeStart;
    LocalDateTime rangeEnd;
    int from;
    int size;

}