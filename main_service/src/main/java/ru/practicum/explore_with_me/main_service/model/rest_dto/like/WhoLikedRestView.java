package ru.practicum.explore_with_me.main_service.model.rest_dto.like;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import ru.practicum.explore_with_me.main_service.model.rest_dto.user.UserRestView;

import java.util.Set;

@Value
@Builder(toBuilder = true)
public class WhoLikedRestView {
    @JsonProperty("whoLiked")
    Set<UserRestView> whoLiked;
    @JsonProperty("whoDisliked")
    Set<UserRestView> whoDisliked;

}