package ru.practicum.explore_with_me.main_service.model.rest_dto.compilation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.EventRestViewShort;

import java.util.Set;

@Value
@Builder(toBuilder = true)
public class CompilationRestView {
    @JsonProperty("id")
    long id;
    @JsonProperty("title")
    String title;
    @JsonProperty("pinned")
    Boolean pinned;
    @JsonProperty("events")
    Set<EventRestViewShort> events;

}