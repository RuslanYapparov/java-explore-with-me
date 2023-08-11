package ru.practicum.explore_with_me.main_service.model.domain_pojo;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Category {
    long id;
    String name;

}