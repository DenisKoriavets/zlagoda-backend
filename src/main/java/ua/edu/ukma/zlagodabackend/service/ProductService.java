package ua.edu.ukma.zlagodabackend.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.edu.ukma.zlagodabackend.dao.ProductDao;
import ua.edu.ukma.zlagodabackend.dto.product.ProductRequest;
import ua.edu.ukma.zlagodabackend.exception.ResourceNotFoundException;
import ua.edu.ukma.zlagodabackend.model.Product;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductDao productDao;
    private final CategoryService categoryService;

    public List<Product> findAllSortedByName() {
        return productDao.findAllSortedByName();
    }

    public List<Product> findByCategoryIdSortedByName(Integer categoryId) {
        categoryService.findById(categoryId);
        return productDao.findByCategoryIdSortedByName(categoryId);
    }

    public List<Product> searchByName(String namePart) {
        return productDao.searchByName(namePart);
    }

    public Product findById(Integer id) {
        return productDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Товар з ID " + id + " не знайдено"));
    }

    public Product create(ProductRequest request) {
        categoryService.findById(request.categoryNumber());

        Product product = new Product();
        product.setCategoryNumber(request.categoryNumber());
        product.setProductName(request.productName());
        product.setCharacteristics(request.characteristics());

        return productDao.save(product);
    }

    public Product update(Integer id, ProductRequest request) {
        Product existingProduct = findById(id);

        if (!existingProduct.getCategoryNumber().equals(request.categoryNumber())) {
            categoryService.findById(request.categoryNumber());
        }

        existingProduct.setCategoryNumber(request.categoryNumber());
        existingProduct.setProductName(request.productName());
        existingProduct.setCharacteristics(request.characteristics());

        productDao.update(existingProduct);
        return existingProduct;
    }

    public void delete(Integer id) {
        findById(id);
        productDao.deleteById(id);
    }
}