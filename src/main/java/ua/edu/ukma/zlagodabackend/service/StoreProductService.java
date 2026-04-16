package ua.edu.ukma.zlagodabackend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.ukma.zlagodabackend.dao.SaleDao;
import ua.edu.ukma.zlagodabackend.dao.StoreProductDao;
import ua.edu.ukma.zlagodabackend.dto.storeProduct.StoreProductCashierResponse;
import ua.edu.ukma.zlagodabackend.dto.storeProduct.StoreProductFullResponse;
import ua.edu.ukma.zlagodabackend.dto.storeProduct.StoreProductRequest;
import ua.edu.ukma.zlagodabackend.exception.BusinessValidationException;
import ua.edu.ukma.zlagodabackend.exception.ResourceNotFoundException;
import ua.edu.ukma.zlagodabackend.model.StoreProduct;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StoreProductService {

    private final StoreProductDao storeProductDao;
    private final ProductService productService;
    private final SaleDao saleDao;

    private static final BigDecimal PROMO_DISCOUNT_FACTOR = new BigDecimal("0.8");

    public List<StoreProductFullResponse> findAllSortedByName() {
        return storeProductDao.findAllSortedByName();
    }

    // Менеджер п. 10: Усі товари у магазині, посортовані за кількістю
    public List<StoreProductFullResponse> findAllSortedByQuantity() {
        return storeProductDao.findAllSortedByQuantity();
    }

    // Менеджер п. 15 / Касир п. 12: Акційні товари (сортування за назвою)
    public List<StoreProductFullResponse> findPromotionalSortedByName() {
        return storeProductDao.findPromotionalSortedByName();
    }

    // Менеджер п. 15 / Касир п. 12: Акційні товари (сортування за кількістю)
    public List<StoreProductFullResponse> findPromotionalSortedByQuantity() {
        return storeProductDao.findPromotionalSortedByQuantity();
    }

    // Менеджер п. 16 / Касир п. 13: Не акційні товари (сортування за назвою)
    public List<StoreProductFullResponse> findNonPromotionalSortedByName() {
        return storeProductDao.findNonPromotionalSortedByName();
    }

    // Менеджер п. 16 / Касир п. 13: Не акційні товари (сортування за кількістю)
    public List<StoreProductFullResponse> findNonPromotionalSortedByQuantity() {
        return storeProductDao.findNonPromotionalSortedByQuantity();
    }

    // Менеджер п. 14: Повна інформація за UPC
    public StoreProductFullResponse findFullDetailsByUpc(String upc) {
        return storeProductDao.findFullDetailsByUpc(upc)
                .orElseThrow(() -> new ResourceNotFoundException("UPC " + upc + " не знайдено"));
    }

    // Касир п. 14: Обмежена інформація за UPC (CQRS-підхід)
    public StoreProductCashierResponse findCashierInfoByUpc(String upc) {
        return storeProductDao.findCashierInfoByUpc(upc)
                .orElseThrow(() -> new ResourceNotFoundException("UPC " + upc + " не знайдено"));
    }

    @Transactional
    public StoreProduct create(StoreProductRequest request) {
        productService.findById(request.idProduct());

        if (Boolean.TRUE.equals(request.promotionalProduct())) {
            return createPromotionalProduct(request);
        } else {
            return processRegularProductRestock(request);
        }
    }

    @Transactional
    public StoreProduct update(String upc, StoreProductRequest request) {
        StoreProduct existing = findEntityByUpc(upc);

        validateConstraints(request, upc);

        existing.setProductsNumber(request.productsNumber());

        if (!existing.getPromotionalProduct()) {
            existing.setSellingPrice(request.sellingPrice());

            storeProductDao.findByProductAndPromo(existing.getIdProduct(), true).ifPresent(promo -> {
                BigDecimal newPromoPrice = request.sellingPrice().multiply(PROMO_DISCOUNT_FACTOR);
                promo.setSellingPrice(newPromoPrice);
                storeProductDao.update(promo);
            });
        } else {
            storeProductDao.findByProductAndPromo(existing.getIdProduct(), false)
                    .ifPresentOrElse(
                            regular -> existing.setSellingPrice(regular.getSellingPrice().multiply(PROMO_DISCOUNT_FACTOR)),
                            () -> existing.setSellingPrice(request.sellingPrice())
                    );
        }

        storeProductDao.update(existing);
        return existing;
    }

    @Transactional
    public void delete(String upc) {
        StoreProduct sp = findEntityByUpc(upc);

        if (saleDao.countRowsByUpc(upc) > 0) {
            throw new BusinessValidationException("Неможливо видалити товар за UPC: він фігурує в реальних чеках.");
        }

        storeProductDao.deleteByUpc(upc);
    }

    // Менеджер п. 21: Загальна кількість одиниць певного товару, проданого за період
    public int getTotalSoldQuantity(String upc, LocalDateTime from, LocalDateTime to) {
        return saleDao.getTotalQuantitySold(upc, from, to);
    }


    private void validateConstraints(StoreProductRequest request, String currentUpc) {
        storeProductDao.findByProductAndPromo(request.idProduct(), request.promotionalProduct())
                .ifPresent(existing -> {
                    if (currentUpc == null || !existing.getUpc().equals(currentUpc)) {
                        String type = request.promotionalProduct() ? "акційна" : "звичайна";
                        throw new BusinessValidationException(
                                "Для цього продукту вже існує " + type + " позиція (UPC: " + existing.getUpc() + "). " +
                                        "Обмеження моделі: максимум 2 позиції на товар (1 звичайна + 1 акційна).");
                    }
                });

        if (request.upc() != null && request.upc().length() > 12) {
            throw new BusinessValidationException("UPC не може перевищувати 12 символів.");
        }

        if (request.sellingPrice() != null && request.sellingPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessValidationException("Ціна продажу не може бути від'ємною.");
        }
        if (request.productsNumber() != null && request.productsNumber() < 0) {
            throw new BusinessValidationException("Кількість товарів не може бути від'ємною.");
        }
    }

    @Transactional
    protected StoreProduct processRegularProductRestock(StoreProductRequest request) {
        Optional<StoreProduct> existingRegular = storeProductDao.findByProductAndPromo(request.idProduct(), false);

        if (existingRegular.isPresent()) {
            StoreProduct reg = existingRegular.get();
            if (!reg.getUpc().equals(request.upc())) {
                throw new BusinessValidationException("Використовуйте UPC: " + reg.getUpc() + " для цього товару.");
            }
            reg.setSellingPrice(request.sellingPrice());
            reg.setProductsNumber(reg.getProductsNumber() + request.productsNumber());
            storeProductDao.update(reg);

            storeProductDao.findByProductAndPromo(reg.getIdProduct(), true).ifPresent(promo -> {
                promo.setSellingPrice(request.sellingPrice().multiply(new BigDecimal("0.8")));
                storeProductDao.update(promo);
            });
            return reg;
        }

        StoreProduct newReg = new StoreProduct(
                request.upc(),
                null,
                request.idProduct(),
                request.sellingPrice(),
                request.productsNumber(),
                false
        );

        storeProductDao.findByProductAndPromo(request.idProduct(), true)
                .ifPresent(promo -> newReg.setUpcProm(promo.getUpc()));

        storeProductDao.save(newReg);
        return newReg;
    }

    @Transactional
    protected StoreProduct createPromotionalProduct(StoreProductRequest request) {
        storeProductDao.findByProductAndPromo(request.idProduct(), true).ifPresent(p -> {
            throw new BusinessValidationException("Акційна позиція для товару " + request.idProduct()
                    + " вже існує (UPC: " + p.getUpc() + ")");
        });

        Optional<StoreProduct> regularOpt = storeProductDao.findByProductAndPromo(request.idProduct(), false);

        BigDecimal finalPrice;
        if (regularOpt.isPresent()) {
            finalPrice = regularOpt.get().getSellingPrice().multiply(new BigDecimal("0.8"));
        } else {
            if (request.sellingPrice() == null) throw new BusinessValidationException("Необхідно вказати ціну для створення акційної позиції.");
            finalPrice = request.sellingPrice();
        }

        StoreProduct promo = new StoreProduct(
                request.upc(),
                null,
                request.idProduct(),
                finalPrice,
                request.productsNumber(),
                true
        );
        storeProductDao.save(promo);

        regularOpt.ifPresent(reg -> {
            reg.setUpcProm(promo.getUpc());
            storeProductDao.update(reg);
        });

        return promo;
    }

    public StoreProduct findEntityByUpc(String upc) {
        return storeProductDao.findEntityByUpc(upc)
                .orElseThrow(() -> new ResourceNotFoundException("Товар у магазині з UPC " + upc + " не знайдено"));
    }
}