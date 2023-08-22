package ru.practicum.explore_with_me.main_service.model.rest_dto.compilation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

@Value
@Builder(toBuilder = true)
public class CompilationRestCommand {
    @NotNull
    @NotBlank
    @Size(min = 1, max = 50)
    @JsonProperty("title")
    String title;
    @JsonProperty("pinned")
    Boolean pinned;
    @JsonProperty("events")
    Set<Long> eventsIds;

}