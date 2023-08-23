package ru.practicum.explore_with_me.main_service.mapper.impl;

import org.mapstruct.Mapper;

import ru.practicum.explore_with_me.main_service.mapper.ObjectMapper;
import ru.practicum.explore_with_me.main_service.model.db_entities.LikeEntity;
import ru.practicum.explore_with_me.main_service.model.db_entities.UserEntity;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.User;
import ru.practicum.explore_with_me.main_service.model.rest_dto.like.WhoLikedRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.user.UserRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.user.UserRestView;

import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

@Mapper(componentModel = "spring")
public interface UserMapper extends ObjectMapper<UserEntity, User, UserRestCommand, UserRestView> {

    default WhoLikedRestView mapWhoLikedRestViewFromListOfLikeEntities(List<LikeEntity> likeEntities) {
        WhoLikedRestView whoLikedRestView = WhoLikedRestView.builder()
                .whoLiked(new TreeSet<>(Comparator.comparingLong(UserRestView::getId)))
                .whoDisliked(new TreeSet<>(Comparator.comparingLong(UserRestView::getId)))
                .build();

        likeEntities.forEach(likeEntity -> {
            if (likeEntity.isLike()) {
                whoLikedRestView.getWhoLiked().add(toRestView(fromDbEntity(likeEntity.getUser())));
            } else {
                whoLikedRestView.getWhoDisliked().add(toRestView(fromDbEntity(likeEntity.getUser())));
            }
        });
        return whoLikedRestView;
    }

}