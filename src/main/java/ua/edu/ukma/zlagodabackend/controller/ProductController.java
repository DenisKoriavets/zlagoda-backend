package ua.edu.ukma.zlagodabackend.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ua.edu.ukma.zlagodabackend.dto.ProductRequest;
import ua.edu.ukma.zlagodabackend.model.Product;
import ua.edu.ukma.zlagodabackend.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public List<Product> getProducts(
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) String search) {
        
        if (category != null) {
            return productService.findByCategoryIdSortedByName(category);
        } else if (search != null && !search.trim().isEmpty()) {
            return productService.searchByName(search.trim());
        } else {
            return productService.findAllSortedByName();
        }
    }

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Integer id) {
        return productService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    // @PreAuthorize("hasRole('MANAGER')")
    public Product createProduct(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @PutMapping("/{id}")
    // @PreAuthorize("hasRole('MANAGER')")
    public Product updateProduct(@PathVariable Integer id, @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    // @PreAuthorize("hasRole('MANAGER')")
    public void deleteProduct(@PathVariable Integer id) {
        productService.delete(id);
    }
}