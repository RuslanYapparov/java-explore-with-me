package ru.practicum.explore_with_me.main_service.controller.for_all;

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
import ru.practicum.explore_with_me.main_service.model.rest_dto.compilation.CompilationRestView;
import ru.practicum.explore_with_me.main_service.service.*;
import ru.practicum.explore_with_me.stats_service.client_submodule.StatsClient;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PublicCompilationController.class)
public class PublicCompilationControllerTest {
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    CompilationService compilationService;
    @MockBean
    UserService userService;
    @MockBean
    EventService eventService;
    @MockBean
    RequestService requestService;
    @MockBean
    CategoryService categoryService;
    @MockBean
    StatsClient statsClient;
    @Autowired
    private MockMvc mvc;

    @Test
    public void getAllCompilations_whenGetCorrectOrNullParameters_thenReturnListOfCompilationRestViews() throws Exception {
        List<CompilationRestView> listOfCompilations = List.of(CompilationRestView.builder()
                .id(1L)
                .title("compilation")
                .events(new HashSet<>())
                .pinned(true)
                .build());
        when(compilationService.getAllCompilations(Mockito.any(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(listOfCompilations);

        mvc.perform(get("/compilations")
                        .param("pinned", "true")
                        .param("from", "0")
                        .param("size", "10")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(objectMapper.readValue(
                        objectMapper.writeValueAsString(listOfCompilations), List.class))));

        mvc.perform(get("/compilations")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(objectMapper.readValue(
                        objectMapper.writeValueAsString(listOfCompilations), List.class))));

        verify(compilationService, Mockito.times(2))
                .getAllCompilations(Mockito.any(), Mockito.anyInt(), Mockito.anyInt());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1L", "0.1234", "foo", "0.1234F", "/", " ", "\n", "\r", "\t", "true"})
    public void getAllCompilations_whenGetIncorrectRequestParameters_thenThrowException(String value) throws Exception {
        when(compilationService.getAllCompilations(Mockito.any(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(Collections.emptyList());

        mvc.perform(get("/compilations")
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

        mvc.perform(get("/compilations")
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

        verify(compilationService, Mockito.never())
                .getAllCompilations(Mockito.any(), Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    public void getCompilationById_whenGetCorrectParameter_thenReturnOkStatus() throws Exception {
        when(compilationService.getCompilationById(Mockito.anyLong()))
                .thenReturn(CompilationRestView.builder()
                        .id(1L)
                        .title("compilation")
                        .events(new HashSet<>())
                        .pinned(true)
                        .build());

        mvc.perform(get("/compilations/{compilation_id}", "1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1L), Long.class))
                .andExpect(jsonPath("$.title", is("compilation")))
                .andExpect(jsonPath("$.pinned", is(true)))
                .andExpect(jsonPath("$.events", is(objectMapper.readValue(
                        objectMapper.writeValueAsString(Collections.emptyList()), List.class))));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1L", "0.1234", "foo", "0.1234F", " ", "\n", "\r", "\t", "true"})
    public void getCompilationById_whenGetIncorrectParameter_thenThrowException(String value) throws Exception {


        mvc.perform(get("/compilations/{compilation_id}", value)
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