package ru.practicum.explore_with_me.main_service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import ru.practicum.explore_with_me.main_service.exception.ObjectNotFoundException;
import ru.practicum.explore_with_me.main_service.mapper.impl.CategoryMapper;
import ru.practicum.explore_with_me.main_service.model.db_entities.CategoryEntity;
import ru.practicum.explore_with_me.main_service.model.domain_pojo.Category;
import ru.practicum.explore_with_me.main_service.model.rest_dto.category.CategoryRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.category.CategoryRestView;
import ru.practicum.explore_with_me.main_service.repository.CategoryRepository;
import ru.practicum.explore_with_me.main_service.service.CategoryService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Service
@Validated
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryRestView saveNewCategory(@Valid CategoryRestCommand categoryRestCommand) {
        CategoryEntity categoryEntity = categoryMapper.toDbEntity(categoryMapper.fromRestCommand(categoryRestCommand));
        categoryEntity = categoryRepository.save(categoryEntity);
        Category category = categoryMapper.fromDbEntity(categoryEntity);
        log.info("New category with name '{}' was saved. Assigned an identifier '{}'",
                category.getName(), category.getId());
        return categoryMapper.toRestView(category);
    }

    public Page<CategoryRestView> getAllCategories(@PositiveOrZero int from, @Positive int size) {
        Pageable page = PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "id"));
        Page<CategoryEntity> categoriesPage = categoryRepository.findAll(page);
        log.info("Page of {} categories started with index {} was sent to the client",
                categoriesPage.getTotalElements(), from);
        return categoriesPage.map(categoryEntity -> categoryMapper.toRestView(categoryMapper.fromDbEntity(categoryEntity)));
    }

    public CategoryRestView getCategoryById(@Positive long categoryId) {
        CategoryEntity categoryEntity = categoryRepository.findById(categoryId).orElseThrow(() ->
                new ObjectNotFoundException("Failed to get category with id'{}': category is not found"));
        log.info("Category {} with id'{}' was sent to client", categoryEntity.getName(), categoryEntity.getId());
        return categoryMapper.toRestView(categoryMapper.fromDbEntity(categoryEntity));
    }

    public CategoryRestView updateCategory(@Positive long categoryId, @Valid CategoryRestCommand categoryRestCommand) {
        CategoryEntity categoryEntity = categoryRepository.findById(categoryId).orElseThrow(() ->
                new ObjectNotFoundException("Failed to update category with id'{}': category is not found"));
        String oldCategoryName = categoryEntity.getName();
        categoryEntity.setName(categoryRestCommand.getName());
        categoryEntity = categoryRepository.save(categoryEntity);
        log.info("Category {} with id'{}' was changed. New category name is {}",
                oldCategoryName, categoryEntity.getId(), categoryEntity.getName());
        return categoryMapper.toRestView(categoryMapper.fromDbEntity(categoryEntity));
    }

    public void deleteCategoryById(@Positive long categoryId) {
        CategoryEntity categoryEntity = categoryRepository.findById(categoryId).orElseThrow(() ->
                new ObjectNotFoundException("Failed to delete category with id'{}': category is not found"));
        categoryRepository.deleteById(categoryId);
        log.info("Category {} with id'{}' was deleted", categoryEntity.getName(), categoryEntity.getId());
    }

}