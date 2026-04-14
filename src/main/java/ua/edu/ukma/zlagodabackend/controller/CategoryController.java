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
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping
    public List<Category> getAllCategories(@RequestParam(required = false) String search) {
        if (search != null && !search.isBlank()) {
            return categoryService.searchByName(search.trim());
        }
        return categoryService.findAllSortedByName();
    }

    @GetMapping("/{id}")
    public Category getCategoryById(@PathVariable Integer id) {
        return categoryService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('MANAGER')")
    public Category createCategory(@Valid @RequestBody CategoryRequest request) {
        return categoryService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public Category updateCategory(@PathVariable Integer id, @Valid @RequestBody CategoryRequest request) {
        return categoryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('MANAGER')")
    public void deleteCategory(@PathVariable Integer id) {
        categoryService.delete(id);
    }
}