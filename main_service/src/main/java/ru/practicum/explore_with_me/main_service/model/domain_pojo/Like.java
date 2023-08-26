package ru.practicum.explore_with_me.main_service.model.domain_pojo;

import lombok.Builder;
import lombok.Value;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
public class Like {
    BigInteger id;
    long user;
    long event;
    boolean isLike;
    LocalDateTime clickedOn;

}