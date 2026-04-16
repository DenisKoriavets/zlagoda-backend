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

    public List<StoreProductDetailsDto> searchByNameOrUpcAllSortedByName(String query) {
        return storeProductDao.findAllWithFilters(null, null, validateSearchQuery(query), "name");
    }

    public List<StoreProductDetailsDto> searchByNameOrUpcAllSortedByQuantity(String query) {
        return storeProductDao.findAllWithFilters(null, null, validateSearchQuery(query), "quantity");
    }

    public List<StoreProductDetailsDto> searchByNameOrUpcPromotionalSortedByName(String query) {
        return storeProductDao.findAllWithFilters(true, null, validateSearchQuery(query), "name");
    }

    public List<StoreProductDetailsDto> searchByNameOrUpcPromotionalSortedByQuantity(String query) {
        return storeProductDao.findAllWithFilters(true, null, validateSearchQuery(query), "quantity");
    }

    public List<StoreProductDetailsDto> searchByNameOrUpcNonPromotionalSortedByName(String query) {
        return storeProductDao.findAllWithFilters(false, null, validateSearchQuery(query), "name");
    }

    public List<StoreProductDetailsDto> searchByNameOrUpcNonPromotionalSortedByQuantity(String query) {
        return storeProductDao.findAllWithFilters(false, null, validateSearchQuery(query), "quantity");
    }

    private static String validateSearchQuery(String query) {
        if (query == null || query.isBlank()) {
            throw new BusinessValidationException("Пошуковий рядок не може бути порожнім.");
        }
        return query.trim();
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
        validateRequestRecursiveFields(request);
        productService.findById(request.idProduct());

        BigDecimal finalPrice;

        if (request.promotionalProduct()) {
            validateMaxTwoPerProduct(request.idProduct(), true, null);
            Optional<StoreProduct> regularOpt = storeProductDao.findByProductAndPromo(request.idProduct(), false);
            if (regularOpt.isEmpty()) {
                throw new BusinessValidationException(
                    "Щоб створити акційний товар, спочатку має існувати звичайний товар для цього продукту.");
            }
            StoreProduct regular = regularOpt.get();
            finalPrice = regular.getSellingPrice().multiply(new BigDecimal("0.8"));
            if (regular.getUpcProm() != null) {
                throw new BusinessValidationException("Для цього товару вже прив'язаний акційний UPC.");
            }

            StoreProduct sp = new StoreProduct(
                    request.upc(), null, request.idProduct(),
                    finalPrice, request.productsNumber(), true);
            storeProductDao.save(sp);

            regular.setUpcProm(request.upc());
            storeProductDao.update(regular);
            validateRecursiveIntegrity(sp);
            validateRecursiveIntegrity(regular);

            return sp;
        } else {
            if (request.sellingPrice() == null || request.sellingPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessValidationException("Ціна продажу є обов'язковою для звичайного товару та має бути > 0.");
            }
            Optional<StoreProduct> existingRegular = storeProductDao.findByProductAndPromo(request.idProduct(), false);
            if (existingRegular.isPresent()) {
                StoreProduct reg = existingRegular.get();
                if (!reg.getUpc().equals(request.upc())) {
                    throw new BusinessValidationException(
                            "Для цього товару вже є звичайна позиція в магазині (UPC: " + reg.getUpc() + "). "
                                    + "Нова партія з новою ціною: вкажіть той самий UPC — додана кількість підсумується, "
                                    + "увесь наявний залишок переоцінюється на нову ціну продажу.");
                }
                int mergedQty = reg.getProductsNumber() + request.productsNumber();
                reg.setSellingPrice(request.sellingPrice());
                reg.setProductsNumber(mergedQty);
                storeProductDao.update(reg);

                storeProductDao.findByProductAndPromo(request.idProduct(), true).ifPresent(promo -> {
                    BigDecimal newPromoPrice = request.sellingPrice().multiply(new BigDecimal("0.8"));
                    promo.setSellingPrice(newPromoPrice);
                    storeProductDao.update(promo);
                    validateRecursiveIntegrity(promo);
                });
                validateRecursiveIntegrity(reg);
                return reg;
            }

            validateMaxTwoPerProduct(request.idProduct(), false, null);
            finalPrice = request.sellingPrice();
            StoreProduct sp = new StoreProduct(
                    request.upc(), null, request.idProduct(),
                    finalPrice, request.productsNumber(), false);
            storeProductDao.save(sp);

            storeProductDao.findByProductAndPromo(request.idProduct(), true).ifPresent(promo -> {
                if (promo.getIdProduct().equals(sp.getIdProduct())) {
                    sp.setUpcProm(promo.getUpc());
                    storeProductDao.update(sp);
                }
                BigDecimal newPromoPrice = finalPrice.multiply(new BigDecimal("0.8"));
                promo.setSellingPrice(newPromoPrice);
                storeProductDao.update(promo);
            });
            validateRecursiveIntegrity(sp);

            return sp;
        }
    }

    @Transactional
    public StoreProduct update(String upc, StoreProductRequest request) {
        StoreProduct existing = findEntityByUpc(upc);
        validateRequestRecursiveFields(request);
        validateRecursiveIntegrity(existing);

        if (!existing.getIdProduct().equals(request.idProduct())) {
            productService.findById(request.idProduct());
            validateSafeProductChange(existing, request.idProduct());
        }

        existing.setProductsNumber(request.productsNumber());
        existing.setIdProduct(request.idProduct());

        if (!existing.getPromotionalProduct()) {
            BigDecimal newPrice = request.sellingPrice() != null ? request.sellingPrice() : existing.getSellingPrice();
            if (newPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessValidationException("Ціна продажу для звичайного товару має бути > 0.");
            }
            existing.setSellingPrice(newPrice);
            storeProductDao.update(existing);

            storeProductDao.findByProductAndPromo(existing.getIdProduct(), true).ifPresent(promo -> {
                promo.setSellingPrice(newPrice.multiply(new BigDecimal("0.8")));
                storeProductDao.update(promo);
                validateRecursiveIntegrity(promo);
            });
        } else {
            existing.setUpcProm(null);
            storeProductDao.update(existing);
        }

        validateRecursiveIntegrity(existing);

        return existing;
    }

    public int getTotalSoldQuantity(String upc, LocalDateTime from, LocalDateTime to) {
        findEntityByUpc(upc);
        return saleDao.getTotalQuantitySold(upc, from, to);
    }

    public void delete(String upc) {
        StoreProduct existing = findEntityByUpc(upc);
        int sales = saleDao.countRowsByUpc(upc);
        if (sales > 0) {
            throw new BusinessValidationException(
                    "Неможливо видалити позицію: цей UPC є в історії продажів (чеки не змінюються).");
        }
        if (!existing.getPromotionalProduct() && existing.getUpcProm() != null) {
            throw new BusinessValidationException(
                    "Неможливо видалити звичайний UPC, поки до нього прив'язаний акційний UPC. "
                            + "Спочатку видаліть або змініть акційний.");
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

    private void validateRequestRecursiveFields(StoreProductRequest request) {
        if (request.upc() == null || request.upc().isBlank()) {
            return;
        }
        if (request.upc().equals(request.upcProm())) {
            throw new BusinessValidationException("UPC не може посилатися сам на себе через upcProm.");
        }
        if (Boolean.TRUE.equals(request.promotionalProduct()) && request.upcProm() != null) {
            throw new BusinessValidationException("Для акційного товару поле upcProm має бути порожнім.");
        }
    }

    private void validateRecursiveIntegrity(StoreProduct storeProduct) {
        if (Boolean.TRUE.equals(storeProduct.getPromotionalProduct())) {
            if (storeProduct.getUpcProm() != null) {
                throw new BusinessValidationException("Акційний UPC не може мати upcProm.");
            }
            Optional<StoreProduct> regular = storeProductDao.findRegularLinkedToPromo(storeProduct.getUpc());
            if (regular.isPresent() && !regular.get().getIdProduct().equals(storeProduct.getIdProduct())) {
                throw new BusinessValidationException("Пара звичайний/акційний UPC повинна належати одному id_product.");
            }
            return;
        }

        String linkedPromoUpc = storeProduct.getUpcProm();
        if (linkedPromoUpc == null || linkedPromoUpc.isBlank()) {
            return;
        }
        StoreProduct linkedPromo = findEntityByUpc(linkedPromoUpc);
        if (!Boolean.TRUE.equals(linkedPromo.getPromotionalProduct())) {
            throw new BusinessValidationException("upcProm має посилатися тільки на акційний UPC.");
        }
        if (!linkedPromo.getIdProduct().equals(storeProduct.getIdProduct())) {
            throw new BusinessValidationException("Пара звичайний/акційний UPC повинна належати одному id_product.");
        }
    }

    private void validateSafeProductChange(StoreProduct existing, Integer newProductId) {
        if (Boolean.TRUE.equals(existing.getPromotionalProduct())) {
            Optional<StoreProduct> regular = storeProductDao.findRegularLinkedToPromo(existing.getUpc());
            if (regular.isPresent()) {
                throw new BusinessValidationException(
                        "Неможливо змінити id_product для акційного UPC, який уже прив'язаний до звичайного.");
            }
            return;
        }

        if (existing.getUpcProm() != null) {
            throw new BusinessValidationException(
                    "Неможливо змінити id_product для звичайного UPC, поки до нього прив'язаний акційний.");
        }
        validateMaxTwoPerProduct(newProductId, existing.getPromotionalProduct(), existing.getUpc());
    }
}