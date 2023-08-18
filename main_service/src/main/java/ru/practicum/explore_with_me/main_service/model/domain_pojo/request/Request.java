package ru.practicum.explore_with_me.main_service.model.domain_pojo.request;

import lombok.Builder;
import lombok.Value;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
public class Request {
    BigInteger id;
    long requester;
    long event;
    LocalDateTime createdOn;
    RequestStatus status;

}