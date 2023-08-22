package ru.practicum.explore_with_me.main_service.model.rest_dto.category;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@NoArgsConstructor
@Getter
@Setter
public class CategoryRestCommand {
    @NotNull
    @NotBlank
    @Size(min = 1, max = 50)
    @JsonProperty("name")
    private String name;

}