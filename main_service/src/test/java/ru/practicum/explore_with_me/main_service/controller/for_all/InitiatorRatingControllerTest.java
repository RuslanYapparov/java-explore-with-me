package ru.practicum.explore_with_me.main_service.controller.for_all;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import ru.practicum.explore_with_me.main_service.model.rest_dto.user.UserRestView;
import ru.practicum.explore_with_me.main_service.service.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InitiatorRatingController.class)
@ContextConfiguration(classes = { InitiatorRatingController.class })
public class InitiatorRatingControllerTest {
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    UserService userService;
    @Autowired
    private MockMvc mvc;

    @Test
    public void getInitiatorsSortedByRating_whenGetCorrectOrNullParameters_thenReturnListOfUserRestViews()
            throws Exception {
        UserRestView user = UserRestView.builder()
                .id(1L)
                .name("user")
                .email("email")
                .rating(100.0F)
                .build();
        when(userService.getInitiatorsSortedByRating(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyBoolean()))
                .thenReturn(List.of(user));

        mvc.perform(get("/initiators/rating")
                        .param("from", "0")
                        .param("size", "10")
                        .param("asc", "true")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(objectMapper.readValue(
                        objectMapper.writeValueAsString(List.of(user)), List.class))));

        mvc.perform(get("/initiators/rating")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(objectMapper.readValue(
                        objectMapper.writeValueAsString(List.of(user)), List.class))));

        verify(userService, Mockito.times(2))
                .getInitiatorsSortedByRating(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyBoolean());
    }

}