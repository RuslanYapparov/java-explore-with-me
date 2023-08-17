package ru.practicum.explore_with_me.main_service.service;

import ru.practicum.explore_with_me.main_service.model.rest_dto.user.UserRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.user.UserRestView;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

public interface UserService {

    UserRestView saveNewUser(@Valid UserRestCommand userRestCommand);

    List<UserRestView> getUsersByIds(long[] ids, @PositiveOrZero int from, @Positive int size);

    void deleteUserById(@Positive long id);

}