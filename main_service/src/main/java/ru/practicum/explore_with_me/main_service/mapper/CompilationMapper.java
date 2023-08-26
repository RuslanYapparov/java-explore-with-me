package ru.practicum.explore_with_me.main_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ru.practicum.explore_with_me.main_service.model.db_entities.CompilationEntity;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.Compilation;
import ru.practicum.explore_with_me.main_service.model.rest_dto.compilation.CompilationRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.compilation.CompilationRestView;

@Mapper(componentModel = "spring")
public interface CompilationMapper {

    Compilation fromRestCommand(CompilationRestCommand compilationRestCommand);

    CompilationRestView toRestView(Compilation compilation);

    Compilation fromDbEntity(CompilationEntity compilationEntity);

    @Mapping(target = "events", ignore = true)
    CompilationEntity toDbEntity(Compilation compilation);

}