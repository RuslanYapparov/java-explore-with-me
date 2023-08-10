package ru.practicum.explore_with_me.main_service.model.domain_pojo;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class User {
    long id;
    String name;
    String email;

}