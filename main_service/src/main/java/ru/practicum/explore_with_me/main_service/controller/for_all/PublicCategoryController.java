package ru.practicum.explore_with_me.main_service.controller.for_all;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore_with_me.main_service.model.rest_dto.category.CategoryRestView;
import ru.practicum.explore_with_me.main_service.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/categories")
@Slf4j
@RequiredArgsConstructor
public class PublicCategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryRestView> getAllCategories(@RequestParam(name = "from", defaultValue = "0") int from,
                                                  @RequestParam(name = "size", defaultValue = "10") int size) {
        log.debug("New request for page of categories with size '{}' from index '{}' was received", size, from);
        return categoryService.getAllCategories(from, size).toList();
    }

    @GetMapping("{category_id}")
    public void getCategoryById(@PathVariable(name = "category_id") long categoryId) {
        log.debug("New request to get category with id'{}' was received", categoryId);
        categoryService.getCategoryById(categoryId);
    }

}