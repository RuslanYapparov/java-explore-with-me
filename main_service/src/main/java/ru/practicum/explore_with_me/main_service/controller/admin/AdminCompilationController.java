package ru.practicum.explore_with_me.main_service.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ru.practicum.explore_with_me.main_service.model.rest_dto.compilation.CompilationRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.compilation.CompilationRestView;
import ru.practicum.explore_with_me.main_service.service.CompilationService;

@RestController
@RequestMapping("/admin/compilations")
@Slf4j
@RequiredArgsConstructor
public class AdminCompilationController {
    private final CompilationService compilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationRestView saveNewCompilation(@RequestBody CompilationRestCommand compilationRestCommand) {
        log.debug("New request to save compilation with title '{}' was received. Events identifiers in compilation: {}",
                compilationRestCommand.getTitle(), compilationRestCommand.getEventsIds());
        return compilationService.saveNewCompilation(compilationRestCommand);
    }

    @PatchMapping("{compilation_id}")
    public CompilationRestView updateNewCompilation(@PathVariable(name = "compilation_id") long compilationId,
                                              @RequestBody CompilationRestCommand compilationRestCommand) {
        log.debug("New request to update compilation with id{} was received. Updated compilation details: '{}'",
                compilationId, compilationRestCommand);
        return compilationService.updateCompilation(compilationId, compilationRestCommand);
    }

    @DeleteMapping("{compilation_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilationById(@PathVariable(name = "compilation_id") long compilationId) {
        log.debug("New request to delete compilation with id'{}' was received", compilationId);
        compilationService.deleteCompilationById(compilationId);
    }

}