package ru.practicum.explore_with_me.main_service.service;

import org.springframework.data.domain.Page;
import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import ru.practicum.explore_with_me.main_service.model.rest_dto.category.CategoryRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.category.CategoryRestView;

public interface CategoryService {

    CategoryRestView saveNewCategory(@Valid CategoryRestCommand categoryRestCommand);

    Page<CategoryRestView> getAllCategories(@PositiveOrZero int from, @Positive int size);

    CategoryRestView getCategoryById(@Positive long categoryId);

    CategoryRestView updateCategory(@Positive long categoryId, @Valid CategoryRestCommand categoryRestCommand);

    void deleteCategoryById(@Positive long categoryId);

}