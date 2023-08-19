package ru.practicum.explore_with_me.main_service.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explore_with_me.main_service.model.rest_dto.compilation.CompilationRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.compilation.CompilationRestView;
import ru.practicum.explore_with_me.main_service.service.*;
import ru.practicum.explore_with_me.stats_service.client_submodule.StatsClient;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminCompilationController.class)
public class AdminCompilationControllerTest {
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
    public void saveNewCompilation_whenGetCorrectParameters_thenReturnCompilationRestView() throws Exception {
        when(compilationService.saveNewCompilation(Mockito.any(CompilationRestCommand.class)))
                .thenReturn(CompilationRestView.builder()
                        .id(1L)
                        .title("compilation")
                        .events(new HashSet<>())
                        .pinned(true)
                        .build());

        mvc.perform(post("/admin/compilations")
                        .content(objectMapper.writeValueAsString(CompilationRestCommand.builder().build()))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1L), Long.class))
                .andExpect(jsonPath("$.title", is("compilation")))
                .andExpect(jsonPath("$.pinned", is(true)));

        verify(compilationService, Mockito.times(1))
                .saveNewCompilation(Mockito.any(CompilationRestCommand.class));
    }

}