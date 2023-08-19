package ru.practicum.explore_with_me.main_service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.practicum.explore_with_me.main_service.exception.ObjectNotFoundException;
import ru.practicum.explore_with_me.main_service.mapper.impl.CompilationMapper;
import ru.practicum.explore_with_me.main_service.model.db_entities.CompilationEntity;
import ru.practicum.explore_with_me.main_service.model.db_entities.event.EventEntity;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.Compilation;
import ru.practicum.explore_with_me.main_service.model.rest_dto.compilation.CompilationRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.compilation.CompilationRestView;
import ru.practicum.explore_with_me.main_service.repository.CompilationRepository;
import ru.practicum.explore_with_me.main_service.repository.EventRepository;
import ru.practicum.explore_with_me.main_service.service.CompilationService;
import ru.practicum.explore_with_me.main_service.util.MethodParameterValidator;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Transactional
@Service
@Validated
@RequiredArgsConstructor
@Slf4j
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;

    @Override
    public CompilationRestView saveNewCompilation(@Valid CompilationRestCommand compilationRestCommand) {
        MethodParameterValidator.checkCompilationRestCommandForSpecificLogic(compilationRestCommand);
        Compilation compilation = compilationMapper.fromRestCommand(compilationRestCommand);
        Set<Long> eventsIds = compilation.getEventsIds();
        Set<EventEntity> events = eventRepository.findAllByIdIn(eventsIds == null ? new HashSet<>() : eventsIds);
        CompilationEntity compilationEntity = compilationMapper.toDbEntity(compilation);
        compilationEntity.setEvents(events);
        compilationEntity = compilationRepository.save(compilationEntity);
        CompilationEntity finalCompilationEntity = compilationEntity;
        events.forEach(eventEntity -> {
            Set<CompilationEntity> compilations = eventEntity.getCompilations();
            if (compilations != null) {
                compilations.add(finalCompilationEntity);
            } else {
                compilations = new HashSet<>();
                compilations.add(finalCompilationEntity);
            }
            eventEntity.setCompilations(compilations);
        });
        eventRepository.saveAll(new ArrayList<>(events));
        // Перевод в ArrayList для избегания ConcurrentModificationException
        compilation = compilationMapper.fromDbEntity(compilationEntity);
        log.info("New {} was saved", compilation);
        return compilationMapper.toRestView(compilation);
    }

    @Override
    public List<CompilationRestView> getAllCompilations(Boolean pinned, @PositiveOrZero int from, @Positive int size) {
        Pageable page = PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "id"));
        List<CompilationEntity> compilationEntities;
        if (pinned == null) {
            compilationEntities = compilationRepository.findAll(page).toList();
        } else {
            compilationEntities = compilationRepository.findAllByPinned(pinned, page).toList();
        }
        log.info("List of {} compilations was sent to client. Page params: from={}, size={}",
                compilationEntities.size(), from, size);
        return compilationEntities.stream()
                .map(compilationEntity ->
                        compilationMapper.toRestView(compilationMapper.fromDbEntity(compilationEntity)))
                .collect(Collectors.toList());
    }

    @Override
    public CompilationRestView getCompilationById(@Positive long compId) {
        CompilationEntity compilationEntity = getCompilationIfExists(compId);
        Compilation compilation = compilationMapper.fromDbEntity(compilationEntity);
        log.info("{} was sent to client", compilation);
        return compilationMapper.toRestView(compilation);
    }

    @Override
    public CompilationRestView updateCompilation(@Positive long compId, CompilationRestCommand compilationRestCommand) {
        MethodParameterValidator.checkCompilationRestCommandForSpecificLogic(compilationRestCommand);
        CompilationEntity compilationEntity = getCompilationIfExists(compId);
        Set<Long> eventsIds = compilationRestCommand.getEventsIds();
        Set<EventEntity> events = eventsIds == null ?
                compilationEntity.getEvents() : eventRepository.findAllByIdIn(eventsIds);
        String title = compilationRestCommand.getTitle();
        Boolean pinned = compilationRestCommand.getPinned();

        compilationEntity.setEvents(events);
        compilationEntity.setTitle(title == null ? compilationEntity.getTitle() : title);
        compilationEntity.setPinned(pinned == null ? compilationEntity.isPinned() : pinned);
        compilationEntity = compilationRepository.save(compilationEntity);
        CompilationEntity finalCompilationEntity = compilationEntity;
        events.forEach(eventEntity -> eventEntity.getCompilations().add(finalCompilationEntity));
        eventRepository.saveAll(new ArrayList<>(events));
        // Перевод в ArrayList для избегания ConcurrentModificationException
        Compilation compilation = compilationMapper.fromDbEntity(compilationEntity);
        log.info("Compilation with id'{}' was updated. Updated {}", compId, compilation);
        return compilationMapper.toRestView(compilation);
    }

    @Override
    public void deleteCompilationById(@Positive long compId) {
        CompilationEntity compilationEntity = getCompilationIfExists(compId);
        compilationRepository.deleteById(compId);
        log.info("{} was deleted", compilationMapper.fromDbEntity(compilationEntity));
    }

    private CompilationEntity getCompilationIfExists(long compilationId) {
        return compilationRepository.findById(compilationId).orElseThrow(() -> new ObjectNotFoundException(
                "Failed to make operation with compilation: the compilation with id'"
                        + compilationId + "' was not saved"));
    }

}