package ru.practicum.explore_with_me.main_service.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import ru.practicum.explore_with_me.main_service.exception.BadRequestParameterException;
import ru.practicum.explore_with_me.main_service.exception.ObjectNotFoundException;
import ru.practicum.explore_with_me.main_service.model.rest_dto.category.CategoryRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.category.CategoryRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.EventRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.EventRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.event.GeoLocation;
import ru.practicum.explore_with_me.main_service.model.rest_dto.compilation.CompilationRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.compilation.CompilationRestView;
import ru.practicum.explore_with_me.main_service.model.rest_dto.user.UserRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.user.UserRestView;
import ru.practicum.explore_with_me.stats_service.client_submodule.StatsClient;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.EwmConstants;
import ru.practicum.explore_with_me.stats_service.dto_submodule.dto.UriStatRestView;

import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CompilationServiceTest {
    private final CompilationService compilationService;
    private final UserService userService;
    private final EventService eventService;
    private final CategoryService categoryService;
    @MockBean
    StatsClient statsClient;

    private static final LocalDateTime DEFAULT_EVENT_DATE = EwmConstants.DEFAULT_DATE_TIME.plusMonths(2);
    private static final GeoLocation DEFAULT_LOCATION = GeoLocation.builder()
            .latitude(7.7D)
            .longitude(7.7D)
            .build();
    private EventRestView firstEvent;
    private EventRestView secondEvent;
    private CompilationRestView firstCompilation;
    private CompilationRestView secondCompilation;

    @BeforeEach
    public void prepareDbForTest_saveNewCompilation_whenGetCorrectCompilationRestCommand_thenReturnCompilationRestView() {
        when(statsClient.getUriStats(Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class),
                Mockito.any(String[].class), Mockito.anyBoolean()))
                .thenReturn(ResponseEntity.ok(new UriStatRestView[] {}));

        UserRestView firstUser = userService.saveNewUser(UserRestCommand.builder()
                .name("user_1")
                .email("user_1@email.com")
                .build());
        UserRestView secondUser = userService.saveNewUser(UserRestCommand.builder()
                .name("user_2")
                .email("user_2@email.com")
                .build());
        CategoryRestCommand categoryRestCommand = new CategoryRestCommand();
        categoryRestCommand.setName("category");
        CategoryRestView category = categoryService.saveNewCategory(categoryRestCommand);

        assertThat(firstUser, notNullValue());
        assertTrue(firstUser.getId() >= 1L);
        assertThat(secondUser, notNullValue());
        assertTrue(secondUser.getId() >= 2L);
        assertThat(category, notNullValue());
        assertTrue(category.getId() >= 1L);

        firstEvent = eventService.saveNewEvent(firstUser.getId(), EventRestCommand.builder()
                .title("title_1")
                .annotation("annotation_of_first_compilation")
                .description("description_of_first_compilation")
                .eventDate(DEFAULT_EVENT_DATE.format(EwmConstants.FORMATTER))
                .category(category.getId())
                .location(DEFAULT_LOCATION)
                .paid(true)
                .requestModeration(false)
                .participantLimit(10)
                .build());
        secondEvent = eventService.saveNewEvent(secondUser.getId(), EventRestCommand.builder()
                .title("title_2")
                .annotation("annotation_of_second_compilation")
                .description("description_of_second_compilation")
                .eventDate(DEFAULT_EVENT_DATE.format(EwmConstants.FORMATTER))
                .category(category.getId())
                .location(DEFAULT_LOCATION)
                .build());

        assertThat(firstEvent, notNullValue());
        assertThat(secondEvent, notNullValue());
        assertTrue(firstEvent.getId() >= 1);
        assertTrue(secondEvent.getId() >= 2);

        firstCompilation = compilationService.saveNewCompilation(CompilationRestCommand.builder()
                .title("first_compilation_title")
                .pinned(true)
                .build());
        secondCompilation = compilationService.saveNewCompilation(CompilationRestCommand.builder()
                .title("second_compilation_title")
                .eventsIds(new HashSet<>(Set.of(firstEvent.getId())))
                .build());

        assertThat(firstCompilation, notNullValue());
        assertThat(secondCompilation, notNullValue());
        assertThat(firstCompilation.getId(), greaterThanOrEqualTo(1L));
        assertThat(secondCompilation.getId(), greaterThanOrEqualTo(2L));
    }

    @Test
    public void getAllCompilation_whenGetCorrectParameters_thenReturnCompilationRestView() {
        CompilationRestView compilationFromService = compilationService.getCompilationById(firstCompilation.getId());
        assertThat(compilationFromService, notNullValue());
        assertThat(compilationFromService.getEvents(), emptyIterable());
        assertThat(compilationFromService.getTitle(), equalTo("first_compilation_title"));
        assertThat(compilationFromService.getPinned(), equalTo(true));
        CompilationRestView anotherCompilationFromService =
                compilationService.getCompilationById(secondCompilation.getId());
        assertThat(anotherCompilationFromService, notNullValue());
        assertThat(anotherCompilationFromService.getEvents(), iterableWithSize(1));
        assertThat(anotherCompilationFromService.getTitle(), equalTo("second_compilation_title"));
        assertThat(anotherCompilationFromService.getPinned(), equalTo(false));

        verify(statsClient, Mockito.times(2))
                .getUriStats(Mockito.any(LocalDateTime.class), Mockito.any(LocalDateTime.class),
                        Mockito.any(String[].class), Mockito.anyBoolean());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\n", "\r", "\t"})
    @NullSource
    public void saveNewCompilation_whenGetCompilationRestCommandWithEmptyField_thenThrowException(String value) {
        assertThrows(ConstraintViolationException.class, () ->
                compilationService.saveNewCompilation(CompilationRestCommand.builder()
                        .title(value)
                        .build()));
    }

    @Test
    public void saveNewCompilation_whenGetIncorrectCompilationRestCommand_thenThrowException() {
        assertThrows(ConstraintViolationException.class, () ->
                compilationService.saveNewCompilation(CompilationRestCommand.builder().build()));

        assertThrows(ConstraintViolationException.class, () ->
                compilationService.saveNewCompilation(CompilationRestCommand.builder()
                        .title("a".repeat(51))
                        .build()));

        assertThrows(DataIntegrityViolationException.class, () ->
                compilationService.saveNewCompilation(CompilationRestCommand.builder()
                        .title("first_compilation_title")
                        .build()));
    }

    @Test
    public void updateCompilation_whenGetCorrectCompilationRestCommand_thenReturnCompilationRestView() {
        compilationService.updateCompilation(firstCompilation.getId(), CompilationRestCommand.builder()
                .title("new_compilation")
                .build());

        firstCompilation = compilationService.getCompilationById(firstCompilation.getId());
        assertThat(firstCompilation, notNullValue());
        assertThat(firstCompilation.getTitle(), equalTo("new_compilation"));
        assertThat(firstCompilation.getEvents(), iterableWithSize(0));

        firstCompilation = compilationService.updateCompilation(firstCompilation.getId(), CompilationRestCommand.builder()
                .eventsIds(new HashSet<>(Set.of(firstEvent.getId(), secondEvent.getId())))
                .title("new_compilation")
                .build());
        assertThat(firstCompilation, notNullValue());
        assertThat(firstCompilation.getTitle(), equalTo("new_compilation"));
        assertThat(firstCompilation.getEvents(), iterableWithSize(2));
        assertThat(firstCompilation.getPinned(), equalTo(true));

        compilationService.updateCompilation(firstCompilation.getId(), CompilationRestCommand.builder()
                .pinned(false)
                .build());
        firstCompilation = compilationService.getCompilationById(firstCompilation.getId());
        assertThat(firstCompilation.getTitle(), equalTo("new_compilation"));
        assertThat(firstCompilation.getEvents(), iterableWithSize(2));
        assertThat(firstCompilation.getPinned(), equalTo(false));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\n", "\r", "\t"})
    public void updateCompilation_whenGetCompilationRestCommandWithEmptyField_thenThrowException(String value) {
        assertThrows(BadRequestParameterException.class, () ->
                compilationService.updateCompilation(firstCompilation.getId(), CompilationRestCommand.builder()
                        .title(value)
                        .build()));
    }

    @Test
    public void updateCompilation_whenGetIncorrectCompilationRestCommand_thenThrowException() {
        assertThrows(BadRequestParameterException.class, () ->
                compilationService.updateCompilation(firstCompilation.getId(), CompilationRestCommand.builder()
                        .title("a".repeat(51))
                        .build()));

        assertThrows(DataIntegrityViolationException.class, () -> {
                compilationService.updateCompilation(firstCompilation.getId(), CompilationRestCommand.builder()
                        .title("second_compilation_title")
                        .build());
                compilationService.getAllCompilations(null, 0, 10);
                }
        );
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    public void updateCompilation_whenGetNullOrNegativeIdParameter_thenThrowsException(long value) {
        assertThrows(ConstraintViolationException.class, () -> compilationService.updateCompilation(value,
                CompilationRestCommand.builder()
                        .title("another_title")
                        .build()));
    }

    @Test
    public void updateCompilation_whenGetNotExistingIdParameter_thenThrowsException() {
        long notExistingCompilationId = secondCompilation.getId() + 1;
        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class, () ->
                compilationService.updateCompilation(notExistingCompilationId, CompilationRestCommand.builder()
                        .title("another_title")
                        .build()));
        assertThat(exception.getMessage(), equalTo("Failed to make operation with compilation: " +
                "the compilation with id'" + notExistingCompilationId + "' was not saved"));
    }

    @Test
    public void getAllCompilations_whenGetCorrectParameters_thenReturnPageOfCompilations() {
        List<CompilationRestView> pageOfCompilations = compilationService.getAllCompilations(null, 0, 10);
        assertThat(pageOfCompilations, notNullValue());
        assertThat(pageOfCompilations, iterableWithSize(2));
        assertThat(pageOfCompilations.get(0), equalTo(firstCompilation));
        assertThat(pageOfCompilations.get(1), equalTo(secondCompilation));

        compilationService.saveNewCompilation(CompilationRestCommand.builder()
                .title("new_compilation")
                .build());

        pageOfCompilations = compilationService.getAllCompilations(null,0, 10);
        assertThat(pageOfCompilations, iterableWithSize(3));
        assertThat(pageOfCompilations.get(0), equalTo(firstCompilation));
        assertThat(pageOfCompilations.get(2).getTitle(), equalTo("new_compilation"));

        pageOfCompilations = compilationService.getAllCompilations(false,0, 10);
        assertThat(pageOfCompilations, iterableWithSize(2));
        assertThat(pageOfCompilations.get(0), equalTo(secondCompilation));
        assertThat(pageOfCompilations.get(1).getTitle(), equalTo("new_compilation"));
        pageOfCompilations = compilationService.getAllCompilations(true,0, 10);
        assertThat(pageOfCompilations, iterableWithSize(1));
        assertThat(pageOfCompilations.get(0), equalTo(firstCompilation));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    public void getAllCompilations_whenGetIncorrectSizeParameter_thenThrowException(int value) {
        assertThrows(ConstraintViolationException.class, () -> compilationService.getAllCompilations(null, 0, value));
    }

    @Test
    public void getAllCompilations_whenGetIncorrectFromParameter_thenThrowException() {
        assertThrows(ConstraintViolationException.class, () -> compilationService.getAllCompilations(false, -1, 5));
    }

    @Test
    public void getCompilationById_whenGetCorrectParameters_thenReturnCompilation() {
        assertThat(compilationService.getCompilationById(firstCompilation.getId()), equalTo(firstCompilation));

        compilationService.saveNewCompilation(CompilationRestCommand.builder()
                .title("new_compilation")
                .build());
        assertThat(compilationService.getCompilationById(secondCompilation.getId() + 1).getTitle(),
                equalTo("new_compilation"));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    public void getCompilationById_whenGetNullOrNegativeParameter_thenThrowsException(long value) {
        assertThrows(ConstraintViolationException.class, () -> compilationService.getCompilationById(value));
    }

    @Test
    public void getCompilationById_whenGetNotExistingIdParameter_thenThrowsException() {
        long notExistingCompilationId = secondCompilation.getId() + 1;
        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class, () ->
                compilationService.getCompilationById(notExistingCompilationId));
        assertThat(exception.getMessage(), equalTo("Failed to make operation with compilation: " +
                "the compilation with id'" + notExistingCompilationId + "' was not saved"));
    }

    @Test
    public void deleteCompilationById_whenGetCorrectParameters_thenDeleteCompilation() {
        compilationService.deleteCompilationById(firstCompilation.getId());
        List<CompilationRestView> allCompilations = compilationService.getAllCompilations(null, 0, 5);
        assertThat(allCompilations, iterableWithSize(1));
        assertThat(allCompilations.get(0), equalTo(secondCompilation));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    public void deleteCompilationById_whenGetNullOrNegativeParameter_thenThrowsException(long value) {
        assertThrows(ConstraintViolationException.class, () -> compilationService.deleteCompilationById(value));
    }

    @Test
    public void deleteCompilationById_whenGetNotExistingIdParameter_thenThrowsException() {
        long notExistingCompilationId = secondCompilation.getId() + 1;
        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class, () ->
                compilationService.deleteCompilationById(notExistingCompilationId));
        assertThat(exception.getMessage(), equalTo("Failed to make operation with compilation: " +
                "the compilation with id'" + notExistingCompilationId + "' was not saved"));
    }

}