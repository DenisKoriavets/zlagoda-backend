package ua.edu.ukma.zlagodabackend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.ukma.zlagodabackend.dao.ProductDao;
import ua.edu.ukma.zlagodabackend.dao.StoreProductDao;
import ua.edu.ukma.zlagodabackend.dto.product.ProductRequest;
import ua.edu.ukma.zlagodabackend.exception.BusinessValidationException;
import ua.edu.ukma.zlagodabackend.exception.ResourceNotFoundException;
import ua.edu.ukma.zlagodabackend.model.Product;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductDao productDao;
    private final StoreProductDao storeProductDao;
    private final CategoryService categoryService;

    // Менеджер п. 9 / Касир п. 1: Отримати інформацію про усі товари, відсортовані за назвою
    public List<Product> findAllSortedByName() {
        return productDao.findAllSortedByName();
    }

    // Менеджер п. 13 / Касир п. 5: Товари певної категорії, відсортовані за назвою
    public List<Product> findByCategoryIdSortedByName(Integer categoryId) {
        categoryService.findById(categoryId); // Перевірка існування категорії
        return productDao.findByCategoryIdSortedByName(categoryId);
    }

    // Касир п. 4: Пошук товарів за назвою
    public List<Product> searchByName(String namePart) {
        if (namePart == null || namePart.isBlank()) {
            return List.of();
        }
        return productDao.searchByName(namePart.trim());
    }

    public Product findById(Integer id) {
        return productDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Товар з ID " + id + " не знайдено"));
    }

    // Менеджер п. 1: Введення відомостей про новий товар
    @Transactional
    public Product create(ProductRequest request) {
        validateProductData(request);
        categoryService.findById(request.categoryNumber());

        Product product = new Product();
        product.setCategoryNumber(request.categoryNumber());
        product.setProductName(request.productName());
        product.setProducer(request.producer());
        product.setCharacteristics(request.characteristics());

        return productDao.save(product);
    }

    // Менеджер п. 2: Редагувати дані про товари
    @Transactional
    public Product update(Integer id, ProductRequest request) {
        Product existingProduct = findById(id);
        validateProductData(request);

        if (!existingProduct.getCategoryNumber().equals(request.categoryNumber())) {
            categoryService.findById(request.categoryNumber());
        }

        existingProduct.setCategoryNumber(request.categoryNumber());
        existingProduct.setProductName(request.productName());
        existingProduct.setProducer(request.producer());
        existingProduct.setCharacteristics(request.characteristics());

        productDao.update(existingProduct);
        return existingProduct;
    }

    // Менеджер п. 3: Вилучити відомості про товар
    @Transactional
    public void delete(Integer id) {
        findById(id); // Перевірка існування

        // Обмеження цілісності: On Delete No Action
        int inStoreCount = storeProductDao.countByIdProduct(id);
        if (inStoreCount > 0) {
            throw new BusinessValidationException(
                    "Неможливо видалити товар: він прив'язаний до " + inStoreCount + " позицій у магазині (Store Product). "
                            + "Спочатку видаліть ці позиції.");
        }
        productDao.deleteById(id);
    }

    // Внутрішня валідація згідно з обмеженнями реляційної моделі (Додаток 2)
    private void validateProductData(ProductRequest request) {
        if (request.productName() == null || request.productName().length() > 50) {
            throw new BusinessValidationException("Назва товару обов'язкова і не може перевищувати 50 символів ");
        }
        if (request.characteristics() == null || request.characteristics().length() > 100) {
            throw new BusinessValidationException("Характеристики не можуть перевищувати 100 символів ");
        }
    }
}