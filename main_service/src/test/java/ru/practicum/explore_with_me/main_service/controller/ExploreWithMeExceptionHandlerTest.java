package ru.practicum.explore_with_me.main_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import ru.practicum.explore_with_me.main_service.controller.admin.UserController;
import ru.practicum.explore_with_me.main_service.exception.BadRequestBodyException;
import ru.practicum.explore_with_me.main_service.exception.BadRequestParameterException;
import ru.practicum.explore_with_me.main_service.exception.ObjectNotFoundException;
import ru.practicum.explore_with_me.main_service.exception.StatsServiceProblemException;
import ru.practicum.explore_with_me.main_service.model.rest_dto.user.UserRestCommand;
import ru.practicum.explore_with_me.main_service.service.*;
import ru.practicum.explore_with_me.stats_service.client_submodule.StatsClient;

import javax.validation.ConstraintViolationException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
public class ExploreWithMeExceptionHandlerTest {
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    CategoryService categoryService;
    @MockBean
    UserService userService;
    @MockBean
    EventService eventService;
    @MockBean
    RequestService requestService;
    @MockBean
    CompilationService compilationService;
    @MockBean
    StatsClient statsClient;
    @Autowired
    private MockMvc mvc;

    @Test
    public void handleObjectNotFoundInStorageException_whenServiceThrows_thenReturnErrorResponse() throws Exception {
        when(userService.saveNewUser(Mockito.any())).thenThrow(new ObjectNotFoundException("not_found"));

        mvc.perform(post("/admin/users")
                        .content(objectMapper.writeValueAsString(UserRestCommand.builder().build()))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is("NOT_FOUND")))
                .andExpect(jsonPath("$.reason", is("There is no saved object with specified id.")))
                .andExpect(jsonPath("$.message", is("not_found")));

        verify(userService, Mockito.times(1))
                .saveNewUser(Mockito.any(UserRestCommand.class));
    }

    @Test
    public void handleBadRequestParameterException_whenServiceThrows_thenReturnErrorResponse() throws Exception {
        when(userService.saveNewUser(Mockito.any())).thenThrow(new BadRequestParameterException("bad_request_parameter"));

        mvc.perform(post("/admin/users")
                        .content(objectMapper.writeValueAsString(UserRestCommand.builder().build()))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is("Http request was made with error(s).")))
                .andExpect(jsonPath("$.message", is("bad_request_parameter")));

        verify(userService, Mockito.times(1))
                .saveNewUser(Mockito.any(UserRestCommand.class));
    }

    @Test
    public void handleBadRequestBodyException_whenServiceThrows_thenReturnErrorResponse() throws Exception {
        when(userService.saveNewUser(Mockito.any())).thenThrow(new BadRequestBodyException("bad_request_body"));

        mvc.perform(post("/admin/users")
                        .content(objectMapper.writeValueAsString(UserRestCommand.builder().build()))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is("FORBIDDEN")))
                .andExpect(jsonPath("$.reason",
                        is("For the requested operation the conditions are not met.")))
                .andExpect(jsonPath("$.message", is("bad_request_body")));

        verify(userService, Mockito.times(1))
                .saveNewUser(Mockito.any(UserRestCommand.class));
    }

    @Test
    public void handleStatsServiceProblemException_whenServiceThrows_thenReturnErrorResponse() throws Exception {
        when(userService.saveNewUser(Mockito.any())).thenThrow(
                new StatsServiceProblemException("stats_service_problem"));

        mvc.perform(post("/admin/users")
                        .content(objectMapper.writeValueAsString(UserRestCommand.builder().build()))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is("SERVICE_UNAVAILABLE")))
                .andExpect(jsonPath("$.reason",
                        is("There is problem with getting information from Stats_service (see message).")))
                .andExpect(jsonPath("$.message", is("stats_service_problem")));

        verify(userService, Mockito.times(1))
                .saveNewUser(Mockito.any(UserRestCommand.class));
    }

    @Test
    public void handleDataIntegrityViolationException_whenServiceThrows_thenReturnErrorResponse() throws Exception {
        when(userService.saveNewUser(Mockito.any())).thenThrow(new DataIntegrityViolationException("not_integrated"));

        mvc.perform(post("/admin/users")
                        .content(objectMapper.writeValueAsString(UserRestCommand.builder().build()))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is("CONFLICT")))
                .andExpect(jsonPath("$.reason", is("Integrity constraint has been violated (see message).")))
                .andExpect(jsonPath("$.message", is("Failed to create/update object data: not_integrated")));

        verify(userService, Mockito.times(1))
                .saveNewUser(Mockito.any(UserRestCommand.class));
    }

    @Test
    public void handleConstraintViolationException_whenServiceThrows_thenReturnErrorResponse() throws Exception {
        when(userService.saveNewUser(Mockito.any())).thenThrow(
                new ConstraintViolationException(new HashSet<>()));

        mvc.perform(post("/admin/users")
                        .content(objectMapper.writeValueAsString(UserRestCommand.builder().build()))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is(
                        "Incorrectly made request: constraint for parameter was violated (see message).")));

        verify(userService, Mockito.times(1))
                .saveNewUser(Mockito.any(UserRestCommand.class));
    }

    @Test
    public void handleAnotherUnhandledException_whenServiceThrows_thenReturnErrorResponse() throws Exception {
        when(userService.saveNewUser(Mockito.any())).thenThrow(new IllegalStateException("oh, no!"));

        mvc.perform(post("/admin/users")
                        .content(objectMapper.writeValueAsString(UserRestCommand.builder().build()))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is("INTERNAL_SERVER_ERROR")))
                .andExpect(jsonPath("$.reason", is(
                        "Unexpected exception during application work. Please inform the developers.")))
                .andExpect(jsonPath("$.message", is("class java.lang.IllegalStateException: oh, no!")));

        verify(userService, Mockito.times(1))
                .saveNewUser(Mockito.any(UserRestCommand.class));
    }

}