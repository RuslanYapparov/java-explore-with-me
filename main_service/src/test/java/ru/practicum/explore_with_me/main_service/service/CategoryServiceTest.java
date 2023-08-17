package ru.practicum.explore_with_me.main_service.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore_with_me.main_service.exception.ObjectNotFoundException;
import ru.practicum.explore_with_me.main_service.model.rest_dto.category.CategoryRestCommand;
import ru.practicum.explore_with_me.main_service.model.rest_dto.category.CategoryRestView;

import javax.validation.ConstraintViolationException;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CategoryServiceTest {
    private final CategoryService categoryService;

    private CategoryRestView firstCategory;
    private CategoryRestView secondCategory;

    @BeforeEach
    public void prepareDbForTest_saveNewCategory_whenGetCorrectCategoryRestCommand_thenReturnCategoryRestView() {
        firstCategory = categoryService.saveNewCategory(initializeCategoryRestCommand("category_1"));
        secondCategory = categoryService.saveNewCategory(initializeCategoryRestCommand("category_2"));

        assertThat(firstCategory, notNullValue());
        assertThat(secondCategory, notNullValue());
        assertTrue(firstCategory.getId() >= 1);
        assertTrue(secondCategory.getId() >= 2);
        assertThat(firstCategory.getName(), equalTo("category_1"));
        assertThat(secondCategory.getName(), equalTo("category_2"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\n", "\r", "\t"})
    @NullSource
    public void saveNewCategory_whenGetCategoryRestCommandWithEmptyField_thenThrowException(String value) {
        assertThrows(ConstraintViolationException.class, () ->
                categoryService.saveNewCategory(initializeCategoryRestCommand(value)));
    }

    @Test
    public void saveNewCategory_whenGetIncorrectCategoryRestCommand_thenThrowException() {
        assertThrows(ConstraintViolationException.class, () ->
                categoryService.saveNewCategory(new CategoryRestCommand()));

        assertThrows(ConstraintViolationException.class, () ->
                categoryService.saveNewCategory(initializeCategoryRestCommand("a".repeat(51))));

        assertThrows(DataIntegrityViolationException.class, () ->
                categoryService.saveNewCategory(initializeCategoryRestCommand("category_1")));
    }

    @Test
    public void updateCategory_whenGetCorrectCategoryRestCommand_thenReturnCategoryRestView() {
        categoryService.updateCategory(firstCategory.getId(), initializeCategoryRestCommand("new_category"));

        firstCategory = categoryService.getCategoryById(firstCategory.getId());
        assertThat(firstCategory, notNullValue());
        assertThat(firstCategory.getName(), equalTo("new_category"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\n", "\r", "\t"})
    @NullSource
    public void updateCategory_whenGetCategoryRestCommandWithEmptyField_thenThrowException(String value) {
        assertThrows(ConstraintViolationException.class, () ->
                categoryService.updateCategory(firstCategory.getId(), initializeCategoryRestCommand(value)));
    }

    @Test
    public void updateCategory_whenGetIncorrectCategoryRestCommand_thenThrowException() {
        assertThrows(ConstraintViolationException.class, () ->
                categoryService.updateCategory(firstCategory.getId(), new CategoryRestCommand()));

        assertThrows(ConstraintViolationException.class, () ->
                categoryService.updateCategory(firstCategory.getId(), initializeCategoryRestCommand("a".repeat(51))));

        assertThrows(DataIntegrityViolationException.class, () -> {
            categoryService.updateCategory(
                    firstCategory.getId(), initializeCategoryRestCommand("category_2"));
            categoryService.getAllCategories(0, 10);
        });
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    public void updateCategory_whenGetNullOrNegativeIdParameter_thenThrowsException(long value) {
        assertThrows(ConstraintViolationException.class, () -> categoryService.deleteCategoryById(value));
    }

    @Test
    public void updateCategory_whenGetNotExistingIdParameter_thenThrowsException() {
        long notExistingCategoryId = secondCategory.getId() + 1;
        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class, () ->
                categoryService.updateCategory(notExistingCategoryId, initializeCategoryRestCommand("category_3")));
        assertThat(exception.getMessage(), equalTo("Failed to update category with id'" + notExistingCategoryId +
                "': category is not found"));
    }

    @Test
    public void getAllCategories_whenGetCorrectParameters_thenReturnPageOfCategories() {
        List<CategoryRestView> pageOfCategories = categoryService.getAllCategories(0, 10);
        assertThat(pageOfCategories, notNullValue());
        assertThat(pageOfCategories, iterableWithSize(2));
        assertThat(pageOfCategories.get(0), equalTo(firstCategory));
        assertThat(pageOfCategories.get(1), equalTo(secondCategory));

        categoryService.saveNewCategory(initializeCategoryRestCommand("category_3"));

        pageOfCategories = categoryService.getAllCategories(0, 10);
        assertThat(pageOfCategories, iterableWithSize(3));
        assertThat(pageOfCategories.get(0), equalTo(firstCategory));
        assertThat(pageOfCategories.get(2).getName(), equalTo("category_3"));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    public void getAllCategories_whenGetIncorrectSizeParameter_thenThrowException(int value) {
        assertThrows(ConstraintViolationException.class, () -> categoryService.getAllCategories(0, value));
    }

    @Test
    public void getAllCategories_whenGetIncorrectFromParameter_thenThrowException() {
        assertThrows(ConstraintViolationException.class, () -> categoryService.getAllCategories(-1, 5));
    }

    @Test
    public void getCategoryById_whenGetCorrectParameters_thenReturnCategory() {
        assertThat(categoryService.getCategoryById(firstCategory.getId()), equalTo(firstCategory));

        categoryService.saveNewCategory(initializeCategoryRestCommand("category_3"));
        assertThat(categoryService.getCategoryById(secondCategory.getId() + 1).getName(), equalTo("category_3"));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    public void getCategoryById_whenGetNullOrNegativeParameter_thenThrowsException(long value) {
        assertThrows(ConstraintViolationException.class, () -> categoryService.getCategoryById(value));
    }

    @Test
    public void getCategoryById_whenGetNotExistingIdParameter_thenThrowsException() {
        long notExistingCategoryId = secondCategory.getId() + 1;
        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class, () ->
                categoryService.getCategoryById(notExistingCategoryId));
        assertThat(exception.getMessage(), equalTo("Failed to get category with id'" + notExistingCategoryId +
                "': category is not found"));
    }

    @Test
    public void deleteCategoryById_whenGetCorrectParameters_thenDeleteCategory() {
        categoryService.deleteCategoryById(firstCategory.getId());
        List<CategoryRestView> allCategories = categoryService.getAllCategories(0, 5);
        assertThat(allCategories, iterableWithSize(1));
        assertThat(allCategories.get(0), equalTo(secondCategory));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    public void deleteCategoryById_whenGetNullOrNegativeParameter_thenThrowsException(long value) {
        assertThrows(ConstraintViolationException.class, () -> categoryService.deleteCategoryById(value));
    }

    @Test
    public void deleteCategoryById_whenGetNotExistingIdParameter_thenThrowsException() {
        long notExistingCategoryId = secondCategory.getId() + 1;
        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class, () ->
                categoryService.deleteCategoryById(notExistingCategoryId));
        assertThat(exception.getMessage(), equalTo("Failed to delete category with id'" + notExistingCategoryId +
                "': category is not found"));
    }

    private CategoryRestCommand initializeCategoryRestCommand(String name) {
        CategoryRestCommand categoryRestCommand = new CategoryRestCommand();
        categoryRestCommand.setName(name);
        return categoryRestCommand;
    }

}