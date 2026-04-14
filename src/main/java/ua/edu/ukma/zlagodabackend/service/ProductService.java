package ua.edu.ukma.zlagodabackend.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.edu.ukma.zlagodabackend.dao.ProductDao;
import ua.edu.ukma.zlagodabackend.dao.StoreProductDao;
import ua.edu.ukma.zlagodabackend.dto.product.ProductRequest;
import ua.edu.ukma.zlagodabackend.exception.BusinessValidationException;
import ua.edu.ukma.zlagodabackend.exception.ResourceNotFoundException;
import ua.edu.ukma.zlagodabackend.model.Product;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductDao productDao;
    private final StoreProductDao storeProductDao;
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
        return loadProduct(id);
    }

    private Product loadProduct(Integer id) {
        Product p = productDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "\u0422\u043e\u0432\u0430\u0440 \u0437 ID " + id + " \u043d\u0435 \u0437\u043d\u0430\u0439\u0434\u0435\u043d\u043e"));
        enrichStoreProductFlags(p);
        return p;
    }

    private void enrichStoreProductFlags(Product p) {
        p.setHasRegularStoreProduct(storeProductDao.findByProductAndPromo(p.getIdProduct(), false).isPresent());
        p.setHasPromotionalStoreProduct(storeProductDao.findByProductAndPromo(p.getIdProduct(), true).isPresent());
    }

    public Product create(ProductRequest request) {
        categoryService.findById(request.categoryNumber());

        Product product = new Product();
        product.setCategoryNumber(request.categoryNumber());
        product.setProductName(request.productName());
        product.setProducer(request.producer());
        product.setCharacteristics(request.characteristics());

        Product saved = productDao.save(product);
        enrichStoreProductFlags(saved);
        return saved;
    }

    public Product update(Integer id, ProductRequest request) {
        Product existingProduct = loadProduct(id);

        if (!existingProduct.getCategoryNumber().equals(request.categoryNumber())) {
            categoryService.findById(request.categoryNumber());
        }

        existingProduct.setCategoryNumber(request.categoryNumber());
        existingProduct.setProductName(request.productName());
        existingProduct.setProducer(request.producer());
        existingProduct.setCharacteristics(request.characteristics());

        productDao.update(existingProduct);
        return loadProduct(id);
    }

    public void delete(Integer id) {
        loadProduct(id);
        int inStore = storeProductDao.countByIdProduct(id);
        if (inStore > 0) {
            throw new BusinessValidationException(
                    "\u041d\u0435\u043c\u043e\u0436\u043b\u0438\u0432\u043e \u0432\u0438\u0434\u0430\u043b\u0438\u0442\u0438 "
                            + "\u0442\u043e\u0432\u0430\u0440: \u0443 \u043c\u0430\u0433\u0430\u0437\u0438\u043d\u0456 \u0454 "
                            + "\u043f\u043e\u0437\u0438\u0446\u0456\u0457 (UPC) \u0446\u044c\u043e\u0433\u043e \u0442\u043e\u0432\u0430\u0440\u0443. "
                            + "\u0421\u043f\u043e\u0447\u0430\u0442\u043a\u0443 \u0432\u0438\u0434\u0430\u043b\u0456\u0442\u044c \u0457\u0445 "
                            + "\u0443 \u0440\u043e\u0437\u0434\u0456\u043b\u0456 \u00ab\u0422\u043e\u0432\u0430\u0440\u0438 \u0432 "
                            + "\u043c\u0430\u0433\u0430\u0437\u0438\u043d\u0456\u00bb.");
        }
        productDao.deleteById(id);
    }
}
