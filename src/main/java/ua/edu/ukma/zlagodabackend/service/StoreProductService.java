package ua.edu.ukma.zlagodabackend.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.ukma.zlagodabackend.dao.SaleDao;
import ua.edu.ukma.zlagodabackend.dao.StoreProductDao;
import ua.edu.ukma.zlagodabackend.dto.storeProduct.StoreProductDetailsDto;
import ua.edu.ukma.zlagodabackend.dto.storeProduct.StoreProductRequest;
import ua.edu.ukma.zlagodabackend.exception.ResourceNotFoundException;
import ua.edu.ukma.zlagodabackend.model.StoreProduct;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreProductService {

    private final StoreProductDao storeProductDao;
    private final ProductService productService;
    private final SaleDao saleDao;

    public List<StoreProductDetailsDto> findAll(Boolean isPromotional, String sortBy) {
        return storeProductDao.findAllWithFilters(isPromotional, sortBy);
    }

    public StoreProductDetailsDto findDetailsByUpc(String upc) {
        return storeProductDao.findDetailsByUpc(upc)
                .orElseThrow(() -> new ResourceNotFoundException("Товар у магазині з UPC " + upc + " не знайдено"));
    }

    public StoreProduct findEntityByUpc(String upc) {
        return storeProductDao.findEntityByUpc(upc)
                .orElseThrow(() -> new ResourceNotFoundException("Товар з UPC " + upc + " не знайдено"));
    }

    @Transactional
    public StoreProduct create(StoreProductRequest request) {
        productService.findById(request.idProduct());

        BigDecimal finalPrice = request.sellingPrice();
        if (request.promotionalProduct()) {
            finalPrice = finalPrice.multiply(new BigDecimal("0.8"));
        } else {
            storeProductDao.updatePriceForProduct(request.idProduct(), finalPrice, false);
            BigDecimal newPromoPrice = finalPrice.multiply(new BigDecimal("0.8"));
            storeProductDao.updatePriceForProduct(request.idProduct(), newPromoPrice, true);
        }

        StoreProduct sp = new StoreProduct(
                request.upc(),
                request.upcProm(),
                request.idProduct(),
                finalPrice,
                request.productsNumber(),
                request.promotionalProduct()
        );
        storeProductDao.save(sp);

        return sp;
    }

    @Transactional
    public StoreProduct update(String upc, StoreProductRequest request) {
        StoreProduct existing = findEntityByUpc(upc);

        if (!existing.getIdProduct().equals(request.idProduct())) {
            productService.findById(request.idProduct());
        }

        existing.setUpcProm(request.upcProm());
        existing.setIdProduct(request.idProduct());
        existing.setSellingPrice(request.sellingPrice());
        existing.setProductsNumber(request.productsNumber());
        existing.setPromotionalProduct(request.promotionalProduct());

        storeProductDao.update(existing);
        return existing;
    }

    public int getTotalSoldQuantity(String upc, LocalDateTime from, LocalDateTime to) {
        findEntityByUpc(upc);
        return saleDao.getTotalQuantitySold(upc, from, to);
    }

    public void delete(String upc) {
        findEntityByUpc(upc);
        storeProductDao.deleteByUpc(upc);
    }
}