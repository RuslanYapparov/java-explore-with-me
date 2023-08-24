package ru.practicum.explore_with_me.main_service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import ru.practicum.explore_with_me.main_service.exception.BadRequestParameterException;
import ru.practicum.explore_with_me.main_service.exception.ObjectModificationException;
import ru.practicum.explore_with_me.main_service.exception.ObjectNotFoundException;
import ru.practicum.explore_with_me.main_service.mapper.impl.EventMapper;
import ru.practicum.explore_with_me.main_service.mapper.impl.UserMapper;
import ru.practicum.explore_with_me.main_service.model.db_entities.LikeEntity;
import ru.practicum.explore_with_me.main_service.model.db_entities.UserEntity;
import ru.practicum.explore_with_me.main_service.model.db_entities.event.EventEntity;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.event.Event;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.event.EventState;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.EventRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.like.LikeRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.like.LikedEventsRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.like.WhoLikedRestView;
import ru.practicum.explore_with_me.main_service.repository.EventRepository;
import ru.practicum.explore_with_me.main_service.repository.LikeRepository;
import ru.practicum.explore_with_me.main_service.repository.UserRepository;
import ru.practicum.explore_with_me.main_service.service.LikeService;
import ru.practicum.explore_with_me.main_service.util.StatsServiceIntegrator;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
@RequiredArgsConstructor
@Slf4j
public class LikeServiceImpl implements LikeService {
    private final LikeRepository likeRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;
    private final UserMapper userMapper;
    private final StatsServiceIntegrator statsServiceIntegrator;

    @Override
    public EventRestView saveNewLike(@Valid LikeRestCommand likeRestCommand) {
        long userId = likeRestCommand.getUser();
        long eventId = likeRestCommand.getEvent();
        UserEntity userEntity = getUserEntityIfExists(userId);
        EventEntity eventEntity = getEventEntityIfExists(eventId);
        UserEntity initiator = eventEntity.getInitiator();
        initiator.getEvents().remove(eventEntity);
        if (!EventState.PUBLISHED.name().equals(eventEntity.getState())) {
            throw new ObjectModificationException("Failed to put like to event with id'" + eventId +
                    "': event not in PUBLISHED state");
        }
        eventEntity.setRating(likeRestCommand.isLike() ? eventEntity.getRating() + 1 : eventEntity.getRating() - 1);
        eventEntity.setNumberOfLikes(eventEntity.getNumberOfLikes() + 1);
        eventRepository.save(eventEntity);
        initiator.getEvents().add(eventEntity);
        saveInitiatorWithNewRatingAfterLike(initiator);
        LikeEntity likeEntity = new LikeEntity();
        likeEntity.setUser(userEntity);
        likeEntity.setEvent(eventEntity);
        likeEntity.setLike(likeRestCommand.isLike());
        likeEntity.setClickedOn(LocalDateTime.now());
        likeRepository.save(likeEntity);
        Event event = getEventWithViewsFromEventEntity(eventEntity);
        log.info("User with id'{}' clicked {} to event with id'{}'", userId,
                likeRestCommand.isLike() ? "like" : "dislike", eventId);
        return eventMapper.toRestView(event);
    }

    @Override
    public LikedEventsRestView getAllEventsLikedByUser(@Positive long userId) {
        getUserEntityIfExists(userId);
        LikedEventsRestView likedEventsRestView = eventMapper.mapLikedEventsRestViewFromListOfEvents(
                likeRepository.findAllByUserId(userId));
        log.info("Information of all events liked by user with id'{}' was sent to client", userId);
        return likedEventsRestView;
    }

    @Override
    public WhoLikedRestView getAllUsersWhoLikedEventForInitiator(@Positive long initiatorId, @Positive long eventId,
                                                                 boolean afterEvent) {
        UserEntity userEntity = getUserEntityIfExists(initiatorId);
        EventEntity eventEntity = getEventEntityIfExists(eventId);
        if (!userEntity.equals(eventEntity.getInitiator())) {
            throw new BadRequestParameterException(String.format("User with id '%d' is not the initiator of " +
                    "event with id'%d' and can't receive information about it by private API", initiatorId, eventId));
        }
        WhoLikedRestView whoLikedRestView;
        if (afterEvent) {
            whoLikedRestView = userMapper.mapWhoLikedRestViewFromListOfLikeEntities(
                    likeRepository.findAllByEventIdAndEventEventDateBefore(eventId, LocalDateTime.now()));
        } else {
            whoLikedRestView = userMapper.mapWhoLikedRestViewFromListOfLikeEntities(
                    likeRepository.findAllByEventId(eventId));
        }
        log.info("Information of all users who liked event with id'{}' for initiator with id'{}' was sent to client",
                eventId, initiatorId);
        return whoLikedRestView;
    }

    @Override
    public EventRestView removeLikeByUser(@Positive long userId, @Positive long eventId) {
        getUserEntityIfExists(userId);
        LikeEntity likeEntity = getLikeIfExists(userId, eventId);
        boolean isLike = likeEntity.isLike();
        EventEntity eventEntity = likeEntity.getEvent();
        UserEntity initiator = eventEntity.getInitiator();
        initiator.getEvents().remove(eventEntity);
        eventEntity.setNumberOfLikes(eventEntity.getNumberOfLikes() - 1);
        eventEntity.setRating(isLike ? eventEntity.getRating() - 1 : eventEntity.getRating() + 1);
        eventRepository.save(eventEntity);
        initiator.getEvents().add(eventEntity);
        saveInitiatorWithNewRatingAfterLike(initiator);
        likeRepository.delete(likeEntity);
        Event event = getEventWithViewsFromEventEntity(eventEntity);
        log.info("User with id'{}' removed his {} from event with id'{}'", userId,
                isLike ? "like" : "dislike", event.getId());
        return eventMapper.toRestView(event);
    }

    private EventEntity getEventEntityIfExists(long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() ->  new ObjectNotFoundException(
                "Failed to save/cancel/get like: event with id'" + eventId + "' was not saved"));
    }

    private UserEntity getUserEntityIfExists(long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ObjectNotFoundException(
                "Failed to save/cancel/get like: user with id'" + userId + "' was not saved"));
    }

    private LikeEntity getLikeIfExists(long userId, long eventId) {
        return likeRepository.findByUserIdAndEventId(userId, eventId).orElseThrow(() ->
                new ObjectNotFoundException(String.format("Failed to save/cancel/get like: like(dislike) from " +
                    "user with id'%d' to event with id'%d' was not saved", userId, eventId)));
    }

    private Event getEventWithViewsFromEventEntity(EventEntity eventEntity) {
        return eventMapper.fromDbEntity(eventEntity).toBuilder()
                .views(statsServiceIntegrator.getViewsForOneUri("/events/" + eventEntity.getId()))
                .build();
    }

    private void saveInitiatorWithNewRatingAfterLike(UserEntity userEntity) {
        List<EventEntity> eventEntities = userEntity.getEvents().stream()
                .filter(eventEntity -> EventState.PUBLISHED.name().equals(eventEntity.getState()))
                .filter(eventEntity -> eventEntity.getNumberOfLikes() > 0)
                .collect(Collectors.toList());
        int sumOfEventRating = 0;
        int sumOfUsersLiked = 0;
        for (EventEntity eventEntity : eventEntities) {
            sumOfEventRating += eventEntity.getRating();
            sumOfUsersLiked += eventEntity.getNumberOfLikes();
        }
        userEntity.setRating(sumOfUsersLiked != 0 ?
                ((float) sumOfEventRating / (float) sumOfUsersLiked) * 100.00F : 0.0F);
        userRepository.save(userEntity);
    }

}