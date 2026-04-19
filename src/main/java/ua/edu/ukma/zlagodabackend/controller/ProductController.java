package ua.edu.ukma.zlagodabackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ua.edu.ukma.zlagodabackend.dto.product.ProductRequest;
import ua.edu.ukma.zlagodabackend.model.Product;
import ua.edu.ukma.zlagodabackend.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // Менеджер п. 1: Введення відомостей про новий товар
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('MANAGER')")
    public Product createProduct(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    // Менеджер п. 2: Оновлення відомостей про товар
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public Product updateProduct(@PathVariable Integer id, @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    // Менеджер п. 3: Вилучення відомостей про товар
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('MANAGER')")
    public void deleteProduct(@PathVariable Integer id) {
        productService.delete(id);
    }

    // Менеджер п. 9 / Касир п. 1: Отримати інформацію про усі товари, відсортовані за назвою
    @GetMapping("/sorted-by-name")
    @PreAuthorize("hasAnyRole('MANAGER', 'CASHIER')")
    public List<Product> getProductsSortedByName() {
        return productService.findAllSortedByName();
    }

    // Менеджер п. 13 / Касир п. 5: Пошук усіх товарів певної категорії, відсортованих за назвою
    @GetMapping("/category/{categoryId}/sorted-by-name")
    @PreAuthorize("hasAnyRole('MANAGER', 'CASHIER')")
    public List<Product> getProductsByCategorySortedByName(@PathVariable Integer categoryId) {
        return productService.findByCategoryIdSortedByName(categoryId);
    }

    // Касир п. 4: Здійснити пошук товарів за назвою
    @GetMapping("/search/{query}")
    @PreAuthorize("hasAnyRole('MANAGER', 'CASHIER')")
    public List<Product> searchProductsByName(@PathVariable String query) {
        return productService.searchByName(query.trim());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'CASHIER')")
    public Product getProductById(@PathVariable Integer id) {
        return productService.findById(id);
    }
}