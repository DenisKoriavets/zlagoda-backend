package ua.edu.ukma.zlagodabackend.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.edu.ukma.zlagodabackend.dao.CategoryDao;
import ua.edu.ukma.zlagodabackend.dao.ProductDao;
import ua.edu.ukma.zlagodabackend.dto.category.CategoryRequest;
import ua.edu.ukma.zlagodabackend.exception.BusinessValidationException;
import ua.edu.ukma.zlagodabackend.exception.ResourceNotFoundException;
import ua.edu.ukma.zlagodabackend.model.Category;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryDao categoryDao;
    private final ProductDao productDao;

    public List<Category> findAllSortedByName() {
        return categoryDao.findAllSortedByName();
    }

    public List<Category> searchByName(String namePart) {
        return categoryDao.searchByName(namePart);
    }

    public Category findById(Integer id) {
        return categoryDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Категорію з ID " + id + " не знайдено"));
    }

    public Category create(CategoryRequest request) {
        Category category = new Category();
        category.setCategoryName(request.categoryName());
        return categoryDao.save(category);
    }

    public Category update(Integer id, CategoryRequest request) {
        Category existingCategory = findById(id); 
        existingCategory.setCategoryName(request.categoryName());
        
        categoryDao.update(existingCategory);
        return existingCategory;
    }

    public void delete(Integer id) {
        findById(id);
        int products = productDao.countByCategoryNumber(id);
        if (products > 0) {
            throw new BusinessValidationException(
                    "Неможливо видалити категорію: у каталозі є товари цієї категорії. "
                            + "Спочатку видаліть їх або перенесіть в іншу категорію.");
        }
        categoryDao.deleteById(id);
    }
}