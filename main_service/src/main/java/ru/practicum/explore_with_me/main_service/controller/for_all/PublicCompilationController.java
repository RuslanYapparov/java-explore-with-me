package ru.practicum.explore_with_me.main_service.controller.for_all;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import ru.practicum.explore_with_me.main_service.model.rest_dto.compilation.CompilationRestView;
import ru.practicum.explore_with_me.main_service.service.CompilationService;

import java.util.List;

@RestController
@RequestMapping("/compilations")
@Slf4j
@RequiredArgsConstructor
public class PublicCompilationController {
    private final CompilationService compilationService;

    @GetMapping
    public List<CompilationRestView> getAllCompilations(@RequestParam(name = "pinned", required = false) Boolean pinned,
                                                        @RequestParam(name = "from", defaultValue = "0") int from,
                                                        @RequestParam(name = "size", defaultValue = "10") int size) {
        log.debug("New request for page of compilations with size '{}' from index '{}' was received. " +
                "Value of pinned is '{}'", size, from, pinned);
        return compilationService.getAllCompilations(pinned, from, size);
    }

    @GetMapping("{compilation_id}")
    public CompilationRestView getCompilationById(@PathVariable(name = "compilation_id") long compilationId) {
        log.debug("New request to get compilation with id'{}' was received", compilationId);
        return compilationService.getCompilationById(compilationId);
    }

}