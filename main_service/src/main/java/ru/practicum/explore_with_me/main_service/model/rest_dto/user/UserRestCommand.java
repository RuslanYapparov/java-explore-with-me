package ru.practicum.explore_with_me.main_service.model.rest_dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Value
@Builder(toBuilder = true)
public class UserRestCommand {
    @NotNull
    @NotBlank
    @Size(min = 2, max = 250)
    @JsonProperty("name")
    String name;
    @NotNull
    @NotBlank
    @Email
    @Size(min = 6, max = 254)
    @JsonProperty("email")
    String email;

}