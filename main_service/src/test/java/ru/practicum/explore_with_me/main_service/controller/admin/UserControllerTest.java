package ru.practicum.explore_with_me.main_service.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import ru.practicum.explore_with_me.main_service.model.rest_dto.user.UserRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.user.UserRestView;
import ru.practicum.explore_with_me.main_service.service.CategoryService;
import ru.practicum.explore_with_me.main_service.service.EventService;
import ru.practicum.explore_with_me.main_service.service.UserService;
import ru.practicum.explore_with_me.stats_service.client_submodule.StatsClient;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {UserController.class, AdminCategoryController.class})
public class UserControllerTest {
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    UserService userService;
    @MockBean
    CategoryService categoryService;
    @MockBean
    EventService eventService;
    @MockBean
    StatsClient statsClient;
    @Autowired
    private MockMvc mvc;

    @Test
    public void saveNewUser_whenGetCorrectParameters_thenReturnUserRestView() throws Exception {
        when(userService.saveNewUser(Mockito.any(UserRestCommand.class)))
                .thenReturn(UserRestView.builder()
                        .id(1L)
                        .name("user")
                        .email("user@email.com")
                        .build());

        mvc.perform(post("/admin/users")
                        .content(objectMapper.writeValueAsString(UserRestCommand.builder().build()))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1L), Long.class))
                .andExpect(jsonPath("$.name", is("user")))
                .andExpect(jsonPath("$.email", is("user@email.com")));


        verify(userService, Mockito.times(1))
                .saveNewUser(Mockito.any(UserRestCommand.class));
    }

    @Test
    public void saveNewUser_whenGetNullOrIncorrectRequestBody_thenThrowException() throws Exception {
        when(userService.saveNewUser(Mockito.any()))
                .thenReturn(UserRestView.builder()
                        .id(1L)
                        .name("user")
                        .email("user@email.com")
                        .build());

        mvc.perform(post("/admin/users")
                        .content("")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is(
                        "Failed to read request body (content is incorrect).")))
                .andExpect(jsonPath("$.message", is("Required request body is missing: public " +
                        "ru.practicum.explore_with_me.main_service.model.rest_dto.user.UserRestView " +
                        "ru.practicum.explore_with_me.main_service.controller.admin.UserController.saveNewUser(" +
                        "ru.practicum.explore_with_me.main_service.model.rest_dto.user.UserRestCommand)")));

        mvc.perform(post("/admin/users")
                        .content("potato")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is(
                        "Failed to read request body (content is incorrect).")))
                .andExpect(jsonPath("$.message", is("JSON parse error: Unrecognized token 'potato': " +
                        "was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false'); " +
                        "nested exception is com.fasterxml.jackson.core.JsonParseException: Unrecognized token 'potato':" +
                        " was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')\n" +
                        " at [Source: (org.springframework.util.StreamUtils$NonClosingInputStream); " +
                        "line: 1, column: 7]")));

        verify(userService, Mockito.never())
                .saveNewUser(Mockito.any());
    }

    @Test
    public void getUserByIds_whenGetCorrectOrNullParameters_thenReturnListOfUserRestViews() throws Exception {
        List<UserRestView> listOfUsers = List.of(UserRestView.builder()
                .id(1L)
                .name("user")
                .email("user@email.com")
                .build());
        when(userService.getUsersByIds(Mockito.any(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(listOfUsers);

        mvc.perform(get("/admin/users")
                        .param("ids", "1, 2, 3")
                        .param("from", "0")
                        .param("size", "10")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(objectMapper.readValue(
                        objectMapper.writeValueAsString(listOfUsers), List.class))));

        mvc.perform(get("/admin/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(objectMapper.readValue(
                        objectMapper.writeValueAsString(listOfUsers), List.class))));

        verify(userService, Mockito.times(2))
                .getUsersByIds(Mockito.any(), Mockito.anyInt(), Mockito.anyInt());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1L", "0.1234", "foo", "0.1234F", "/", " ", "\n", "\r", "\t", "true"})
    public void getUsersByIds_whenGetIncorrectRequestParameters_thenThrowException(String value) throws Exception {
        when(userService.getUsersByIds(Mockito.any(), Mockito.anyInt(), Mockito.anyInt()))
        .thenReturn(Collections.emptyList());

        mvc.perform(get("/admin/users")
                        .param("ids", value)
                        .param("from", "0")
                        .param("size", "10")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is(
                        "Incorrectly made request: type of one or more parameters is not supported " +
                                "(see message).")));

        mvc.perform(get("/admin/users")
                        .param("ids", "1, 2, 3")
                        .param("from", value)
                        .param("size", "10")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is(
                        "Incorrectly made request: type of one or more parameters is not supported " +
                                "(see message).")));

        mvc.perform(get("/admin/users")
                        .param("ids", "1, 2, 3")
                        .param("from", "0")
                        .param("size", value)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is(
                        "Incorrectly made request: type of one or more parameters is not supported " +
                                "(see message).")));

        verify(userService, Mockito.never())
                .getUsersByIds(Mockito.any(), Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    public void deleteUserById_whenGetCorrectParameter_thenReturnOkStatus() throws Exception {
        mvc.perform(delete("/admin/users/{user_id}", "1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1L", "0.1234", "foo", "0.1234F", " ", "\n", "\r", "\t", "true"})
    public void deleteUserById_whenGetIncorrectParameter_thenThrowException(String value) throws Exception {
        mvc.perform(delete("/admin/users/{user_id}", value)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is(
                        "Incorrectly made request: type of one or more parameters is not supported " +
                                "(see message).")));
    }

}