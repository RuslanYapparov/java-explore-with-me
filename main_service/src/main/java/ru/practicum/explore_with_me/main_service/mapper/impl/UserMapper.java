package ru.practicum.explore_with_me.main_service.mapper.impl;

import org.mapstruct.Mapper;
import ru.practicum.explore_with_me.main_service.mapper.ObjectMapper;
import ru.practicum.explore_with_me.main_service.model.db_entities.UserEntity;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.User;
import ru.practicum.explore_with_me.main_service.model.rest_dto.user.UserRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.user.UserRestView;

@Mapper(componentModel = "spring")
public interface UserMapper extends ObjectMapper<UserEntity, User, UserRestCommand, UserRestView> {

}