package ua.edu.ukma.zlagodabackend.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.ukma.zlagodabackend.dao.SaleDao;
import ua.edu.ukma.zlagodabackend.dao.StoreProductDao;
import ua.edu.ukma.zlagodabackend.dto.storeProduct.StoreProductDetailsDto;
import ua.edu.ukma.zlagodabackend.dto.storeProduct.StoreProductRequest;
import ua.edu.ukma.zlagodabackend.exception.BusinessValidationException;
import ua.edu.ukma.zlagodabackend.exception.ResourceNotFoundException;
import ua.edu.ukma.zlagodabackend.model.StoreProduct;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StoreProductService {

    private final StoreProductDao storeProductDao;
    private final ProductService productService;
    private final SaleDao saleDao;

    public List<StoreProductDetailsDto> findAll(Boolean isPromotional, Integer category, String search, String sortBy) {
        return storeProductDao.findAllWithFilters(isPromotional, category, search, sortBy);
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

        validateMaxTwoPerProduct(request.idProduct(), request.promotionalProduct(), null);

        BigDecimal finalPrice;
        String upcProm = null;

        if (request.promotionalProduct()) {
            Optional<StoreProduct> regularOpt = storeProductDao.findByProductAndPromo(request.idProduct(), false);
            if (regularOpt.isEmpty()) {
                throw new BusinessValidationException(
                    "Щоб створити акційний товар, спочатку має існувати звичайний товар для цього продукту.");
            }
            StoreProduct regular = regularOpt.get();
            finalPrice = regular.getSellingPrice().multiply(new BigDecimal("0.8"));

            StoreProduct sp = new StoreProduct(
                    request.upc(), null, request.idProduct(),
                    finalPrice, request.productsNumber(), true);
            storeProductDao.save(sp);

            regular.setUpcProm(request.upc());
            storeProductDao.update(regular);

            return sp;
        } else {
            if (request.sellingPrice() == null || request.sellingPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessValidationException("Ціна продажу є обов'язковою для звичайного товару та має бути > 0.");
            }
            finalPrice = request.sellingPrice();
            StoreProduct sp = new StoreProduct(
                    request.upc(), null, request.idProduct(),
                    finalPrice, request.productsNumber(), false);
            storeProductDao.save(sp);

            storeProductDao.findByProductAndPromo(request.idProduct(), true).ifPresent(promo -> {
                BigDecimal newPromoPrice = finalPrice.multiply(new BigDecimal("0.8"));
                promo.setSellingPrice(newPromoPrice);
                storeProductDao.update(promo);

                sp.setUpcProm(promo.getUpc());
                storeProductDao.update(sp);
            });

            return sp;
        }
    }

    @Transactional
    public StoreProduct update(String upc, StoreProductRequest request) {
        StoreProduct existing = findEntityByUpc(upc);

        if (!existing.getIdProduct().equals(request.idProduct())) {
            productService.findById(request.idProduct());
        }

        existing.setProductsNumber(request.productsNumber());
        existing.setIdProduct(request.idProduct());

        if (!existing.getPromotionalProduct()) {
            BigDecimal newPrice = request.sellingPrice() != null ? request.sellingPrice() : existing.getSellingPrice();
            existing.setSellingPrice(newPrice);
            storeProductDao.update(existing);

            storeProductDao.findByProductAndPromo(existing.getIdProduct(), true).ifPresent(promo -> {
                promo.setSellingPrice(newPrice.multiply(new BigDecimal("0.8")));
                storeProductDao.update(promo);
            });
        } else {
            storeProductDao.update(existing);
        }

        return existing;
    }

    public int getTotalSoldQuantity(String upc, LocalDateTime from, LocalDateTime to) {
        findEntityByUpc(upc);
        return saleDao.getTotalQuantitySold(upc, from, to);
    }

    public void delete(String upc) {
        findEntityByUpc(upc);
        int sales = saleDao.countRowsByUpc(upc);
        if (sales > 0) {
            throw new BusinessValidationException(
                    "Неможливо видалити позицію: цей UPC є в історії продажів (чеки не змінюються).");
        }
        storeProductDao.deleteByUpc(upc);
    }

    private void validateMaxTwoPerProduct(Integer idProduct, boolean isPromo, String excludeUpc) {
        Optional<StoreProduct> existing = storeProductDao.findByProductAndPromo(idProduct, isPromo);
        if (existing.isPresent() && (excludeUpc == null || !existing.get().getUpc().equals(excludeUpc))) {
            String type = isPromo ? "акційний" : "звичайний";
            throw new BusinessValidationException(
                "Для цього продукту вже існує " + type + " товар у магазині (UPC: " + existing.get().getUpc() + ").");
        }
    }
}