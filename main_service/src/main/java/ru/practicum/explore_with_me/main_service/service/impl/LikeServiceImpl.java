package ru.practicum.explore_with_me.main_service.service.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import ru.practicum.explore_with_me.main_service.model.domain_pojo.params_holder.JpaGetEventAuthorsQueryParamsHolder;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.EventRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.like.HttpGetAuthorsWithRatingRequestParamsHolder;
import ru.practicum.explore_with_me.main_service.model.rest_dto.like.LikeRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.like.LikedEventsRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.like.WhoLikedRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.user.UserRestView;
import ru.practicum.explore_with_me.main_service.repository.EventRepository;
import ru.practicum.explore_with_me.main_service.repository.LikeRepository;
import ru.practicum.explore_with_me.main_service.repository.UserRepository;
import ru.practicum.explore_with_me.main_service.service.LikeService;
import ru.practicum.explore_with_me.main_service.util.MethodParameterValidator;
import ru.practicum.explore_with_me.main_service.util.QueryDslExpressionCreator;
import ru.practicum.explore_with_me.main_service.util.StatsServiceIntegrator;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.math.BigInteger;
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
        if (!EventState.PUBLISHED.name().equals(eventEntity.getState())) {
            throw new ObjectModificationException("Failed to put like to event with id'" + eventId +
                    "': event not in PUBLISHED state");
        }
        eventEntity.setRating(likeRestCommand.isLike() ? eventEntity.getRating() + 1 : eventEntity.getRating() - 1);
        eventEntity.setNumberOfUsersLiked(eventEntity.getNumberOfUsersLiked() + 1);
        eventRepository.save(eventEntity);
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
                    likeRepository.findAllByEventIdAndEventEventDateAfter(eventId, LocalDateTime.now()));
        } else {
            whoLikedRestView = userMapper.mapWhoLikedRestViewFromListOfLikeEntities(
                    likeRepository.findAllByEventId(eventId));
        }
        log.info("Information of all users who liked event with id'{}' for initiator with id'{}' was sent to client",
                eventId, initiatorId);
        return whoLikedRestView;
    }

    @Override
    public EventRestView removeLikeByUser(@Positive long userId, @Positive BigInteger likeId) {
        UserEntity userEntity = getUserEntityIfExists(userId);
        LikeEntity likeEntity = getLikeIfExists(likeId);
        if (!likeEntity.getUser().equals(userEntity)) {
            throw new BadRequestParameterException(String.format("Failed to cancel like(dislike) with id'%d': " +
                    "user with id'%d' don't put this like", likeId, userId));
        }
        boolean isLike = likeEntity.isLike();
        EventEntity eventEntity = likeEntity.getEvent();
        eventEntity.setNumberOfUsersLiked(eventEntity.getNumberOfUsersLiked() - 1);
        eventEntity.setRating(isLike ? eventEntity.getRating() - 1 : eventEntity.getRating() + 1);
        eventRepository.save(eventEntity);
        likeRepository.deleteById(likeId);
        Event event = getEventWithViewsFromEventEntity(eventEntity);
        log.info("User with id'{}' removed his {} from event with id'{}'", userId,
                isLike ? "like" : "dislike", event.getId());
        return eventMapper.toRestView(event);
    }

    @Override
    public List<UserRestView> getAuthorsOfEventsSortedByAverageEventRatings(
            @Positive long userId, @Valid HttpGetAuthorsWithRatingRequestParamsHolder httpParams) {
        getUserEntityIfExists(userId);
        JpaGetEventAuthorsQueryParamsHolder paramsHolder =
                MethodParameterValidator.getValidJpaQueryParamsFromHttpRequest(httpParams);
        BooleanExpression preparedConditions = QueryDslExpressionCreator.prepareConditionsForQuery(paramsHolder);
        Sort.Direction direction = httpParams.isAsc() ? Sort.Direction.ASC : Sort.Direction.DESC;
        int from = httpParams.getFrom();
        int size = httpParams.getSize();
        Sort sort = Sort.by(direction, "event.rating / event.numberOfUsersLiked");
        Pageable page = PageRequest.of(from, size, sort);
        List<UserEntity> users = userRepository.findAll(preparedConditions, page).toList();
        log.info("Requested list of initiators ranked by events ratings sent to client. List size={}. Paging " +
                "parameters: from={}, size={}, direction={}", users.size(), from, size, direction);
        return users.stream()
                .map(userEntityInStream -> userMapper.toRestView(userMapper.fromDbEntity(userEntityInStream)))
                .collect(Collectors.toList());
    }

    private EventEntity getEventEntityIfExists(long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() ->  new ObjectNotFoundException(
                "Failed to save/cancel/get like: event with id'" + eventId + "' was not saved"));
    }

    private UserEntity getUserEntityIfExists(long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ObjectNotFoundException(
                "Failed to save/cancel/get like: user with id'" + userId + "' was not saved"));
    }

    private LikeEntity getLikeIfExists(BigInteger likeId) {
        return likeRepository.findById(likeId).orElseThrow(() -> new ObjectNotFoundException(
                "Failed to save/cancel/get like: like with id'" + likeId + "' was not saved"));
    }

    private Event getEventWithViewsFromEventEntity(EventEntity eventEntity) {
        return eventMapper.fromDbEntity(eventEntity).toBuilder()
                .views(statsServiceIntegrator.getViewsForOneUri("/events/" + eventEntity.getId()))
                .build();
    }

}