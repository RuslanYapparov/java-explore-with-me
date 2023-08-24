package ru.practicum.explore_with_me.main_service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import ru.practicum.explore_with_me.main_service.exception.ObjectNotFoundException;
import ru.practicum.explore_with_me.main_service.mapper.CompilationMapper;
import ru.practicum.explore_with_me.main_service.model.db_entities.CompilationEntity;
import ru.practicum.explore_with_me.main_service.model.db_entities.event.EventEntity;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.Compilation;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.event.Event;
import ru.practicum.explore_with_me.main_service.model.rest_dto.compilation.CompilationRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.compilation.CompilationRestView;
import ru.practicum.explore_with_me.main_service.repository.CompilationRepository;
import ru.practicum.explore_with_me.main_service.repository.EventRepository;
import ru.practicum.explore_with_me.main_service.service.CompilationService;
import ru.practicum.explore_with_me.main_service.util.MethodParameterValidator;
import ru.practicum.explore_with_me.main_service.util.StatsServiceIntegrator;

import java.util.*;
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
    private final StatsServiceIntegrator statsServiceIntegrator;

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
        eventRepository.saveAll(new ArrayList<>(events)); // При передаче в метод множества events напрямую получаем
        // ConcurrentModificationException, поэтому преобразовываю Set в ArrayList
        compilation = getCompilationWithEventsWithViews(compilationEntity);
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
        List<Compilation> compilations = getListOfCompilationsWithEventsWithViews(compilationEntities);
        log.info("List of {} compilations was sent to client. Page params: from={}, size={}",
                compilations.size(), from, size);
        return compilations.stream()
                .map(compilationMapper::toRestView)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationRestView getCompilationById(@Positive long compId) {
        CompilationEntity compilationEntity = getCompilationIfExists(compId);
        Compilation compilation = getCompilationWithEventsWithViews(compilationEntity);
        log.info("{} was sent to client", compilation);
        return compilationMapper.toRestView(compilation);
    }

    @Override
    public CompilationRestView updateCompilation(@Positive long compId, CompilationRestCommand compilationRestCommand) {
        MethodParameterValidator.checkCompilationRestCommandForSpecificLogic(compilationRestCommand);
        CompilationEntity compilationEntity = getCompilationIfExists(compId);
        Set<Long> eventsIds = compilationRestCommand.getEventsIds();
        Set<EventEntity> events;
        if (eventsIds == null) {
            Set<EventEntity> eventsFromCompilation = compilationEntity.getEvents();
            events = eventsFromCompilation == null ? new HashSet<>() : eventsFromCompilation;
        } else {
            events = eventRepository.findAllByIdIn(eventsIds);
        }
        String title = compilationRestCommand.getTitle();
        Boolean pinned = compilationRestCommand.getPinned();

        compilationEntity.setEvents(events);
        compilationEntity.setTitle(title == null ? compilationEntity.getTitle() : title);
        compilationEntity.setPinned(pinned == null ? compilationEntity.isPinned() : pinned);
        compilationEntity = compilationRepository.save(compilationEntity);
        CompilationEntity finalCompilationEntity = compilationEntity;
        events.forEach(eventEntity -> {
            Set<CompilationEntity> compilationsOfEvent = eventEntity.getCompilations();
            if (compilationsOfEvent != null) {
                eventEntity.getCompilations().add(finalCompilationEntity);
            } else {
                eventEntity.setCompilations(new HashSet<>(Set.of(finalCompilationEntity)));
            }
        });
        eventRepository.saveAll(new ArrayList<>(events)); // При передаче в метод множества events напрямую получаем
        // ConcurrentModificationException, поэтому преобразовываю Set в ArrayList
        Compilation compilation = getCompilationWithEventsWithViews(compilationEntity);
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

    private List<Compilation> getListOfCompilationsWithEventsWithViews(List<CompilationEntity> compilationEntities) {
        if (compilationEntities == null || compilationEntities.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, Event> mapOfEvents = new HashMap<>();
        List<Event> allEventsFromCompilationEntitiesWithViews = statsServiceIntegrator
                .mapEventEntitiesToEventsWithViews(compilationEntities.stream()
                        .map(CompilationEntity::getEvents)
                        .flatMap(Set::stream)
                        .collect(Collectors.toList()));
        allEventsFromCompilationEntitiesWithViews.forEach(event ->
                mapOfEvents.put(event.getId(), event));
        return compilationEntities.stream()
                .map(compilationMapper::fromDbEntity)
                .peek(compilation -> compilation.toBuilder()
                        .events(compilation.getEvents().stream()
                                .map(Event::getId)
                                .map(mapOfEvents::get)
                                .collect(Collectors.toSet()))
                        .build())
                .collect(Collectors.toList());
    }

    private Compilation getCompilationWithEventsWithViews(CompilationEntity compilationEntity) {
        if (compilationEntity.getEvents() == null || compilationEntity.getEvents().isEmpty()) {
            return compilationMapper.fromDbEntity(compilationEntity);
        }
        List<Event> allEventsFromCompilationEntityWithViews = statsServiceIntegrator
                .mapEventEntitiesToEventsWithViews(compilationEntity.getEvents());
        return compilationMapper.fromDbEntity(compilationEntity).toBuilder()
                .events(new HashSet<>(allEventsFromCompilationEntityWithViews))
                .build();
    }

}