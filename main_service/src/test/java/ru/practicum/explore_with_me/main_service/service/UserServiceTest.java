package ru.practicum.explore_with_me.main_service.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import javax.validation.ConstraintViolationException;

import ru.practicum.explore_with_me.main_service.exception.ObjectNotFoundException;
import ru.practicum.explore_with_me.main_service.model.rest_dto.user.UserRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.user.UserRestView;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceTest {
    private final UserService userService;

    private UserRestView firstUser;
    private UserRestView secondUser;

    @BeforeEach
    public void prepareDbForTest_saveNewUser_whenGetCorrectUserRestCommand_thenReturnUserRestView() {
        firstUser = userService.saveNewUser(UserRestCommand.builder()
                .name("user_1")
                .email("user_1@email.com")
                .build());
        secondUser = userService.saveNewUser(UserRestCommand.builder()
                .name("user_2")
                .email("user_2@email.com")
                .build());

        assertThat(firstUser, notNullValue());
        assertThat(secondUser, notNullValue());
        assertTrue(firstUser.getId() >= 1);
        assertTrue(secondUser.getId() >= 2);
        assertThat(firstUser.getName(), equalTo("user_1"));
        assertThat(secondUser.getName(), equalTo("user_2"));
        assertThat(firstUser.getEmail(), equalTo("user_1@email.com"));
        assertThat(secondUser.getEmail(), equalTo("user_2@email.com"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\n", "\r", "\t", "a"})
    @NullSource
    public void saveNewUser_whenGetUserRestCommandWithEmptyField_thenThrowException(String value) {
        assertThrows(ConstraintViolationException.class, () ->
                userService.saveNewUser(UserRestCommand.builder().build()));

        assertThrows(ConstraintViolationException.class, () ->
                userService.saveNewUser(UserRestCommand.builder()
                        .name(value)
                        .email("user_email@email.com")
                        .build()));

        assertThrows(ConstraintViolationException.class, () ->
                userService.saveNewUser(UserRestCommand.builder()
                        .name("user")
                        .email(value)
                        .build()));
    }

    @Test
    public void saveNewUser_whenGetIncorrectUserRestCommand_thenThrowException() {
        assertThrows(ConstraintViolationException.class, () ->
                userService.saveNewUser(UserRestCommand.builder()
                        .name("user")
                        .email("a@r.r")
                        .build()));

        assertThrows(ConstraintViolationException.class, () ->
                userService.saveNewUser(UserRestCommand.builder()
                        .name("a".repeat(256))
                        .email("a".repeat(250) + "@yandex.ru")
                        .build()));

        assertThrows(DataIntegrityViolationException.class, () ->
                userService.saveNewUser(UserRestCommand.builder()
                        .name("user_name")
                        .email("user_1@email.com")
                        .build()));
    }

    @Test
    public void getUsersByIds_whenGetCorrectParameters_thenReturnPageOfUsers() {
        Page<UserRestView> pageOfUsers = userService.getUsersByIds(
                new long[] {firstUser.getId(), secondUser.getId()}, 0, 10);
        assertThat(pageOfUsers, notNullValue());
        assertThat(pageOfUsers, iterableWithSize(2));
        assertThat(pageOfUsers.getContent().get(0), equalTo(firstUser));
        assertThat(pageOfUsers.getContent().get(1), equalTo(secondUser));

        assertThat(pageOfUsers, equalTo(userService.getUsersByIds(new long[] {-1L}, 0, 10)));

        assertThat(userService.getUsersByIds(new long[] {-1L, 37582L, -4893L, 0L}, 0, 10), iterableWithSize(0));

        pageOfUsers = userService.getUsersByIds(new long[] {-1L, 37582L, -4893L, 0L, secondUser.getId()}, 0, 10);
        assertThat(pageOfUsers, iterableWithSize(1));
        assertThat(pageOfUsers.getContent().get(0), equalTo(secondUser));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    public void getUsersByIds_whenGetIncorrectSizeParameter_thenThrowException(int value) {
        assertThrows(ConstraintViolationException.class, () -> userService.getUsersByIds(new long[] {0L, 1L, 7L},
                0, value));
    }

    @Test
    public void getUsersByIds_whenGetIncorrectFromParameter_thenThrowException() {
        assertThrows(ConstraintViolationException.class, () -> userService.getUsersByIds(new long[] {0L, 1L, 7L},
                -1, 10));
    }

    @Test
    public void deleteUserById_whenGetCorrectParameters_thenDeleteUser() {
        userService.deleteUserById(firstUser.getId());
        Page<UserRestView> allUsers = userService.getUsersByIds(new long[] {-1}, 0, 5);
        assertThat(allUsers, iterableWithSize(1));
        assertThat(allUsers.getContent().get(0), equalTo(secondUser));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    public void deleteUserById_whenGetNullOrNegativeParameter_thenThrowsException(long value) {
        assertThrows(ConstraintViolationException.class, () -> userService.deleteUserById(value));
    }

    @Test
    public void deleteUserById_whenGetNotExistingIdParameter_thenThrowsException() {
        long notExistingUserId = secondUser.getId() + 1;
        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class, () ->
                userService.deleteUserById(notExistingUserId));
        assertThat(exception.getMessage(), equalTo("Failed to delete user with id'" + notExistingUserId +
                "': user is not found"));
    }

}