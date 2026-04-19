package ua.edu.ukma.zlagodabackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ua.edu.ukma.zlagodabackend.dto.category.CategoryRequest;
import ua.edu.ukma.zlagodabackend.model.Category;
import ua.edu.ukma.zlagodabackend.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // Менеджер (Вимога 8): Отримати інформацію про усі категорії, відсортовані за назвою
    @GetMapping
    public List<Category> getAllCategories() {
        return categoryService.findAllSortedByName();
    }

    // Допоміжний метод для отримання даних однієї категорії (для фронтенду)
    @GetMapping("/{id}")
    public Category getCategoryById(@PathVariable Integer id) {
        return categoryService.findById(id);
    }

    // Менеджер (Вимога 1): Додавати нові дані про категорії товарів
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('MANAGER')")
    public Category createCategory(@Valid @RequestBody CategoryRequest request) {
        return categoryService.create(request);
    }

    // Менеджер (Вимога 2): Редагувати дані про категорії товарів
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public Category updateCategory(@PathVariable Integer id, @Valid @RequestBody CategoryRequest request) {
        return categoryService.update(id, request);
    }

    // Менеджер (Вимога 3): Видаляти дані про категорії товарів
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('MANAGER')")
    public void deleteCategory(@PathVariable Integer id) {
        categoryService.delete(id);
    }
}