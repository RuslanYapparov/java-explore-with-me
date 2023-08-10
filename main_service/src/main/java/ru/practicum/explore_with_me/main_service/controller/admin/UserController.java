package ru.practicum.explore_with_me.main_service.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ru.practicum.explore_with_me.main_service.model.rest_dto.user.UserRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.user.UserRestView;
import ru.practicum.explore_with_me.main_service.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserRestView saveNewUser(@RequestBody UserRestCommand userRestCommand) {
        log.debug("New request to save user with name '{}' was received", userRestCommand.getName());
        return userService.saveNewUser(userRestCommand);
    }

    @GetMapping
    public List<UserRestView> getUsersByIds(@RequestParam(name = "ids", defaultValue = "-1") long[] ids,
                                            @RequestParam(name = "from", defaultValue = "0") int from,
                                            @RequestParam(name = "size", defaultValue = "10") int size) {
        log.debug("New request for page of users with size '{}' from index '{}' was received. " +
                "Requested users ids: {}", size, from, ids);
        return userService.getUsersByIds(ids, from, size).toList();
    }

    @DeleteMapping("{user_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserById(@PathVariable(name = "user_id") long userId) {
        log.debug("New request to delete user with id'{}' was received", userId);
        userService.deleteUserById(userId);
    }

}