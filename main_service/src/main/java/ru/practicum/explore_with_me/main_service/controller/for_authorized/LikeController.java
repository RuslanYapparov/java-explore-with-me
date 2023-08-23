package ru.practicum.explore_with_me.main_service.controller.for_authorized;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ru.practicum.explore_with_me.main_service.model.rest_dto.event.EventRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.like.HttpGetAuthorsWithRatingRequestParamsHolder;
import ru.practicum.explore_with_me.main_service.model.rest_dto.like.LikedEventsRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.like.LikeRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.like.WhoLikedRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.user.UserRestView;
import ru.practicum.explore_with_me.main_service.service.LikeService;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/users/{user_id}")
@RequiredArgsConstructor
@Slf4j
public class LikeController {
    private final LikeService likeService;

    @PostMapping("/likes")
    @ResponseStatus(HttpStatus.CREATED)
    public EventRestView saveNewLike(@PathVariable(name = "user_id") long userId,
                                     @RequestParam(name = "eventId") long eventId) {
        log.debug("New request to save like from user with id'{}' to event with id'{}' was received",
                userId, eventId);
        return likeService.saveNewLike(LikeRestCommand.builder()
                .user(userId)
                .event(eventId)
                .build());
    }

    @GetMapping("/likes")
    public LikedEventsRestView getAllEventsLikedByUser(@PathVariable(name = "user_id") long userId) {
        log.debug("New request from user with id'{}' for information of all events he clicked liked or dislike was received. ",
                userId);
        return likeService.getAllEventsLikedByUser(userId);
    }

    @GetMapping("/events/{event_id}/likes")
    public WhoLikedRestView getAllUsersWhoLikedEventForInitiator(@PathVariable(name = "user_id") long userId,
                                                                 @PathVariable(name = "event_id") long eventId,
                                                                 @RequestParam(name = "afterEvent",
                                                                         defaultValue = "false") boolean afterEvent) {
        log.debug("New request from user with id'{}' for information of all users who liked his event with id'{}' " +
                "was received", userId, eventId);
        return likeService.getAllUsersWhoLikedEventForInitiator(userId, eventId, afterEvent);
    }

    @GetMapping("/rating/authors")
    public List<UserRestView> getEventAuthorsSortedByRating(
            @PathVariable(name = "user_id") long userId,
            @RequestParam(name = "categories", required = false) long[] categories,
            @RequestParam(name = "eventsAreOver", required = false) Boolean eventsAreOver,
            @RequestParam(name = "paid", required = false) Boolean paid,
            @RequestParam(name = "asc", defaultValue = "false") boolean asc,
            @RequestParam(name = "from", defaultValue = "0") int from,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        HttpGetAuthorsWithRatingRequestParamsHolder paramsHolder = HttpGetAuthorsWithRatingRequestParamsHolder
                .builder()
                .categories(categories)
                .eventsAreOver(eventsAreOver)
                .paid(paid)
                .asc(asc)
                .from(from)
                .size(size)
                .build();
        log.debug("New request from user with id'{}' to get list of authors sorted by event rating " +
                "was received. Parameters are: {}", userId, paramsHolder);
        return likeService.getAuthorsOfEventsSortedByAverageEventRatings(userId, paramsHolder);
    }

    @DeleteMapping("/likes/{like_id}/remove")
    public EventRestView removeLike(@PathVariable(name = "user_id") long userId,
                                    @PathVariable(name = "like_id") BigInteger likeId) {
        log.debug("New request to remove like with id'{}' from user with id'{}' was received", likeId, userId);
        return likeService.removeLikeByUser(userId, likeId);
    }

}