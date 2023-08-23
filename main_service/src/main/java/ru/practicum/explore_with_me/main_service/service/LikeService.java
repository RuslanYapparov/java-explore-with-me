package ru.practicum.explore_with_me.main_service.service;

import ru.practicum.explore_with_me.main_service.model.rest_dto.event.EventRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.like.HttpGetAuthorsWithRatingRequestParamsHolder;
import ru.practicum.explore_with_me.main_service.model.rest_dto.like.LikeRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.like.LikedEventsRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.like.WhoLikedRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.user.UserRestView;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.math.BigInteger;
import java.util.List;

public interface LikeService {

    EventRestView saveNewLike(@Valid LikeRestCommand likeRestCommand);

    LikedEventsRestView getAllEventsLikedByUser(@Positive long userId);

    WhoLikedRestView getAllUsersWhoLikedEventForInitiator(@Positive long userId,
                                                          @Positive long eventId, boolean afterEvent);

    EventRestView removeLikeByUser(@Positive long userId, @Positive BigInteger likeId);

    List<UserRestView> getAuthorsOfEventsSortedByAverageEventRatings(
            @Positive long userId, @Valid HttpGetAuthorsWithRatingRequestParamsHolder paramsHolder);

}