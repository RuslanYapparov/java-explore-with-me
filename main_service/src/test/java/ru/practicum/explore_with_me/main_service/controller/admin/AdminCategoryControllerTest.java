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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explore_with_me.main_service.controller.ExploreWithMeExceptionHandler;
import ru.practicum.explore_with_me.main_service.model.rest_dto.category.CategoryRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.category.CategoryRestView;
import ru.practicum.explore_with_me.main_service.service.*;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminCategoryController.class)
@ContextConfiguration(classes = { AdminCategoryController.class, ExploreWithMeExceptionHandler.class })
public class AdminCategoryControllerTest {
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    CategoryService categoryService;
    @Autowired
    private MockMvc mvc;

    @Test
    public void saveNewCategory_whenGetCorrectParameters_thenReturnCategoryRestView() throws Exception {
        when(categoryService.saveNewCategory(Mockito.any(CategoryRestCommand.class)))
                .thenReturn(CategoryRestView.builder()
                        .id(1L)
                        .name("category")
                        .build());

        mvc.perform(post("/admin/categories")
                        .content(objectMapper.writeValueAsString(new CategoryRestCommand()))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1L), Long.class))
                .andExpect(jsonPath("$.name", is("category")));


        verify(categoryService, Mockito.times(1))
                .saveNewCategory(Mockito.any(CategoryRestCommand.class));
    }

    @Test
    public void saveNewCategory_whenGetNullOrIncorrectRequestBody_thenThrowException() throws Exception {
        when(categoryService.saveNewCategory(Mockito.any()))
                .thenReturn(CategoryRestView.builder()
                        .id(1L)
                        .name("category")
                        .build());

        mvc.perform(post("/admin/categories")
                        .content("")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is(
                        "Failed to read request body (content is incorrect).")))
                .andExpect(jsonPath("$.message", is("Required request body is missing: public " +
                        "ru.practicum.explore_with_me.main_service.model.rest_dto.category.CategoryRestView " +
                        "ru.practicum.explore_with_me.main_service.controller.admin.AdminCategoryController" +
                        ".saveNewCategory(ru.practicum.explore_with_me.main_service.model.rest_dto.category" +
                        ".CategoryRestCommand)")));

        mvc.perform(post("/admin/categories")
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

        verify(categoryService, Mockito.never())
                .saveNewCategory(Mockito.any());
    }

    @Test
    public void updateCategory_whenGetCorrectParameters_thenReturnCategoryRestView() throws Exception {
        when(categoryService.updateCategory(Mockito.anyLong(), Mockito.any(CategoryRestCommand.class)))
                .thenReturn(CategoryRestView.builder()
                        .id(1L)
                        .name("category")
                        .build());

        mvc.perform(patch("/admin/categories/{category_id}", "1234")
                        .content(objectMapper.writeValueAsString(new CategoryRestCommand()))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1L), Long.class))
                .andExpect(jsonPath("$.name", is("category")));


        verify(categoryService, Mockito.times(1))
                .updateCategory(Mockito.anyLong(), Mockito.any(CategoryRestCommand.class));
    }

    @Test
    public void updateCategory_whenGetNullOrIncorrectRequestBody_thenThrowException() throws Exception {
        when(categoryService.updateCategory(Mockito.anyLong(), Mockito.any(CategoryRestCommand.class)))
                .thenReturn(CategoryRestView.builder()
                        .id(1L)
                        .name("category")
                        .build());

        mvc.perform(patch("/admin/categories/{category_id}", "1234")
                        .content("")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is(
                        "Failed to read request body (content is incorrect).")))
                .andExpect(jsonPath("$.message", is("Required request body is missing: public " +
                        "ru.practicum.explore_with_me.main_service.model.rest_dto.category.CategoryRestView " +
                        "ru.practicum.explore_with_me.main_service.controller.admin.AdminCategoryController" +
                        ".updateNewCategory(long,ru.practicum.explore_with_me.main_service.model.rest_dto.category" +
                        ".CategoryRestCommand)")));

        mvc.perform(patch("/admin/categories/{category_id}", "1")
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

        verify(categoryService, Mockito.never())
                .updateCategory(Mockito.anyLong(), Mockito.any(CategoryRestCommand.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1L", "0.1234", "foo", "0.1234F", " ", "\n", "\r", "\t", "true"})
    public void updateCategory_whenGetIncorrectIdParameter_thenThrowException(String value) throws Exception {
        when(categoryService.updateCategory(Mockito.anyLong(), Mockito.any(CategoryRestCommand.class)))
                .thenReturn(CategoryRestView.builder()
                        .id(1L)
                        .name("category")
                        .build());

        mvc.perform(patch("/admin/categories/{category_id}", value)
                        .content(objectMapper.writeValueAsString(new CategoryRestCommand()))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("BAD_REQUEST")))
                .andExpect(jsonPath("$.reason", is(
                        "Incorrectly made request: type of one or more parameters is not supported " +
                                "(see message).")));

        verify(categoryService, Mockito.never())
                .updateCategory(Mockito.anyLong(), Mockito.any(CategoryRestCommand.class));
    }

    @Test
    public void deleteCategoryById_whenGetCorrectParameter_thenReturnOkStatus() throws Exception {
        mvc.perform(delete("/admin/categories/{category_id}", "1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1L", "0.1234", "foo", "0.1234F", " ", "\n", "\r", "\t", "true"})
    public void deleteCategoryById_whenGetIncorrectParameter_thenThrowException(String value) throws Exception {
        mvc.perform(delete("/admin/categories/{category_id}", value)
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