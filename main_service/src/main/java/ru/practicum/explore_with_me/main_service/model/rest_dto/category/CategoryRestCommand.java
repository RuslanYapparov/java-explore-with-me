package ru.practicum.explore_with_me.main_service.model.rest_dto.category;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@NoArgsConstructor
@Getter
public class CategoryRestCommand {
    @NotNull
    @NotBlank
    @Size(min = 1, max = 50)
    @JsonProperty("name")
    private String name;

}