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

import ru.practicum.explore_with_me.main_service.model.rest_dto.category.CategoryRestView;
import ru.practicum.explore_with_me.main_service.service.CategoryService;
import ru.practicum.explore_with_me.main_service.service.EventService;
import ru.practicum.explore_with_me.main_service.service.RequestService;
import ru.practicum.explore_with_me.main_service.service.UserService;
import ru.practicum.explore_with_me.stats_service.client_submodule.StatsClient;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PublicCategoryController.class)
public class PublicCategoryControllerTest {
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
    StatsClient statsClient;
    @Autowired
    private MockMvc mvc;

    @Test
    public void getCategoryByIds_whenGetCorrectOrNullParameters_thenReturnListOfCategoryRestViews() throws Exception {
        List<CategoryRestView> listOfCategories = List.of(CategoryRestView.builder()
                .id(1L)
                .name("category")
                .build());
        when(categoryService.getAllCategories(Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(listOfCategories);

        mvc.perform(get("/categories")
                        .param("from", "0")
                        .param("size", "10")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(objectMapper.readValue(
                        objectMapper.writeValueAsString(listOfCategories), List.class))));

        mvc.perform(get("/categories")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(objectMapper.readValue(
                        objectMapper.writeValueAsString(listOfCategories), List.class))));

        verify(categoryService, Mockito.times(2))
                .getAllCategories(Mockito.anyInt(), Mockito.anyInt());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1L", "0.1234", "foo", "0.1234F", "/", " ", "\n", "\r", "\t", "true"})
    public void getCategoriesByIds_whenGetIncorrectRequestParameters_thenThrowException(String value) throws Exception {
        when(categoryService.getAllCategories(Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(Collections.emptyList());

        mvc.perform(get("/categories")
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

        mvc.perform(get("/categories")
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

        verify(categoryService, Mockito.never())
                .getAllCategories(Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    public void getCategoryById_whenGetCorrectParameter_thenReturnOkStatus() throws Exception {
        when(categoryService.getCategoryById(Mockito.anyLong()))
                .thenReturn(CategoryRestView.builder()
                        .id(1L)
                        .name("category")
                        .build());

        mvc.perform(get("/categories/{category_id}", "1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("category")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1L", "0.1234", "foo", "0.1234F", " ", "\n", "\r", "\t", "true"})
    public void getCategoryById_whenGetIncorrectParameter_thenThrowException(String value) throws Exception {


        mvc.perform(get("/categories/{category_id}", value)
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