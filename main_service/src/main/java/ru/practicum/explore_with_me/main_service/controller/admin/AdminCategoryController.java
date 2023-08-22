package ru.practicum.explore_with_me.main_service.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ru.practicum.explore_with_me.main_service.model.rest_dto.category.CategoryRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.category.CategoryRestView;
import ru.practicum.explore_with_me.main_service.service.CategoryService;

@RestController
@RequestMapping("/admin/categories")
@Slf4j
@RequiredArgsConstructor
public class AdminCategoryController {
    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryRestView saveNewCategory(@RequestBody CategoryRestCommand categoryRestCommand) {
        log.debug("New request to save category with name '{}' was received", categoryRestCommand.getName());
        return categoryService.saveNewCategory(categoryRestCommand);
    }

    @PatchMapping("{category_id}")
    public CategoryRestView updateNewCategory(@PathVariable(name = "category_id") long categoryId,
                                              @RequestBody CategoryRestCommand categoryRestCommand) {
        log.debug("New request to update category with id{} was received. New category name is '{}'",
                categoryId, categoryRestCommand.getName());
        return categoryService.updateCategory(categoryId, categoryRestCommand);
    }

    @DeleteMapping("{category_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategoryById(@PathVariable(name = "category_id") long categoryId) {
        log.debug("New request to delete category with id'{}' was received", categoryId);
        categoryService.deleteCategoryById(categoryId);
    }

}