package ru.practicum.explore_with_me.main_service.service;

import ru.practicum.explore_with_me.main_service.model.rest_dto.event.EventRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.like.LikeRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.like.LikedEventsRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.like.WhoLikedRestView;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

public interface LikeService {

    EventRestView saveNewLike(@Valid LikeRestCommand likeRestCommand);

    LikedEventsRestView getAllEventsLikedByUser(@Positive long userId);

    WhoLikedRestView getAllUsersWhoLikedEventForInitiator(@Positive long userId,
                                                          @Positive long eventId, boolean afterEvent);

    EventRestView removeLikeByUser(@Positive long userId, @Positive long eventId);

}