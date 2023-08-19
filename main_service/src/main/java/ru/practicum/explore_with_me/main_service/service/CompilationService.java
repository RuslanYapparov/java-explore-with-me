package ru.practicum.explore_with_me.main_service.service;

import ru.practicum.explore_with_me.main_service.model.rest_dto.compilation.CompilationRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.compilation.CompilationRestView;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import java.util.List;

public interface CompilationService {

    CompilationRestView saveNewCompilation(@Valid CompilationRestCommand compilationRestCommand);

    List<CompilationRestView> getAllCompilations(Boolean pinned, @PositiveOrZero int from, @Positive int size);

    CompilationRestView getCompilationById(@Positive long compId);

    CompilationRestView updateCompilation(@Positive long compId, CompilationRestCommand compilationRestCommand);

    void deleteCompilationById(@Positive long compId);

}