package ru.practicum.explore_with_me.main_service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import org.springframework.validation.annotation.Validated;

import ru.practicum.explore_with_me.main_service.exception.ObjectNotFoundException;
import ru.practicum.explore_with_me.main_service.mapper.UserMapper;
import ru.practicum.explore_with_me.main_service.model.db_entities.UserEntity;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.User;
import ru.practicum.explore_with_me.main_service.model.rest_dto.user.UserRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.user.UserRestView;
import ru.practicum.explore_with_me.main_service.repository.UserRepository;
import ru.practicum.explore_with_me.main_service.service.UserService;

import java.util.Arrays;
import java.util.List;

@Service
@Validated
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserRestView saveNewUser(@Valid UserRestCommand userRestCommand) {
        UserEntity userEntity = userMapper.toDbEntity(userMapper.fromRestCommand(userRestCommand));
        userEntity = userRepository.save(userEntity);
        User user = userMapper.fromDbEntity(userEntity);
        log.info("New user with name '{}' was saved. Assigned an identifier '{}'", user.getName(), user.getId());
        return userMapper.toRestView(user);
    }

    @Override
    public List<UserRestView> getUsersByIds(long[] ids, @PositiveOrZero int from, @Positive int size) {
        Pageable page = PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "id"));
        Page<UserEntity> usersPage;
        if (ids.length == 1 && ids[0] == -1) {
            usersPage = userRepository.findAll(page);
        } else {
            Arrays.sort(ids);
            usersPage = userRepository.findAllByIdIn(ids, page);
        }
        log.info("Page of {} users with identifiers {} was sent to the client.",
                size, Arrays.copyOfRange(ids, from, size > ids.length ? ids.length : size));
        return usersPage.map(userEntity -> userMapper.toRestView(userMapper.fromDbEntity(userEntity))).toList();
    }

    @Override
    public List<UserRestView> getInitiatorsSortedByRating(@PositiveOrZero int from, @Positive int size, boolean asc) {
        Pageable page;
        if (asc) {
            page = PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "rating"));
        } else {
            page = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "rating"));
        }
        Page<UserEntity> usersPage = userRepository.findAllDistinctByEventsNotNull(page);
        log.info("Page of {} initiators sorted {} by rating was sent to the client. " +
                        "Paging parameters: from={}, size={}",
                usersPage.getTotalElements(), asc ? "ascending" : "descending", from, size);
        return usersPage.map(userEntity -> userMapper.toRestView(userMapper.fromDbEntity(userEntity))).toList();
    }

    @Override
    public void deleteUserById(@Positive long id) {
        UserEntity userEntity = userRepository.findById(id).orElseThrow(() ->
                new ObjectNotFoundException("Failed to delete user with id'" + id + "': user is not found"));
        userRepository.deleteById(id);
        log.info("User with id'{}' was deleted", userEntity.getId());
    }

}