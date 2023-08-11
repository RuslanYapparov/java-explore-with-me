package ru.practicum.explore_with_me.main_service.model.rest_dto.category;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class CategoryRestView {
    @JsonProperty("id")
    long id;
    @JsonProperty("name")
    String name;

}