package ru.practicum.explore_with_me.main_service.mapper;

import org.mapstruct.Mapper;

import ru.practicum.explore_with_me.main_service.model.db_entities.CategoryEntity;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.Category;
import ru.practicum.explore_with_me.main_service.model.rest_dto.category.CategoryRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.category.CategoryRestView;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category fromRestCommand(CategoryRestCommand categoryRestCommand);

    CategoryRestView toRestView(Category category);

    Category fromDbEntity(CategoryEntity categoryEntity);

    CategoryEntity toDbEntity(Category category);

}