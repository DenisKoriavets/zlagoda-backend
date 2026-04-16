package ua.edu.ukma.zlagodabackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ua.edu.ukma.zlagodabackend.dto.storeProduct.StoreProductCashierResponse;
import ua.edu.ukma.zlagodabackend.dto.storeProduct.StoreProductDetailsDto;
import ua.edu.ukma.zlagodabackend.dto.storeProduct.StoreProductFullResponse;
import ua.edu.ukma.zlagodabackend.dto.storeProduct.StoreProductRequest;
import ua.edu.ukma.zlagodabackend.model.StoreProduct;
import ua.edu.ukma.zlagodabackend.service.StoreProductService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/store-products")
@RequiredArgsConstructor
public class StoreProductController {

    private final StoreProductService storeProductService;

    // Менеджер п. 1: Додавати нові дані про товари у магазині
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('MANAGER')")
    public StoreProduct createStoreProduct(@Valid @RequestBody StoreProductRequest request) {
        return storeProductService.create(request);
    }

    // Менеджер п. 2: Редагувати дані про товари у магазині
    @PutMapping("/{upc}")
    @PreAuthorize("hasRole('MANAGER')")
    public StoreProduct updateStoreProduct(
            @PathVariable String upc,
            @Valid @RequestBody StoreProductRequest request) {
        return storeProductService.update(upc, request);
    }

    // Менеджер п. 3: Видаляти дані про товари у магазині
    @DeleteMapping("/{upc}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('MANAGER')")
    public void deleteStoreProduct(@PathVariable String upc) {
        storeProductService.delete(upc);
    }

    // Касир п. 2: Отримати усі товари у магазині, відсортовані за назвою
    @GetMapping("/sorted-by-name")
    @PreAuthorize("hasAnyRole('MANAGER', 'CASHIER')")
    public List<StoreProductFullResponse> getStoreProductsSortedByName() {
        return storeProductService.findAllSortedByName();
    }

    // Менеджер п. 10: Отримати усі товари у магазині, відсортовані за кількістю
    @GetMapping("/sorted-by-quantity")
    @PreAuthorize("hasAnyRole('MANAGER', 'CASHIER')")
    public List<StoreProductFullResponse> getStoreProductsSortedByQuantity() {
        return storeProductService.findAllSortedByQuantity();
    }

    // Менеджер п. 15 / Касир п. 12: Акційні товари, відсортовані за назвою
    @GetMapping("/promotional/sorted-by-name")
    @PreAuthorize("hasAnyRole('MANAGER', 'CASHIER')")
    public List<StoreProductFullResponse> getPromotionalStoreProductsSortedByName() {
        return storeProductService.findPromotionalSortedByName();
    }

    // Менеджер п. 15 / Касир п. 12: Акційні товари, відсортовані за кількістю
    @GetMapping("/promotional/sorted-by-quantity")
    @PreAuthorize("hasAnyRole('MANAGER', 'CASHIER')")
    public List<StoreProductFullResponse> getPromotionalStoreProductsSortedByQuantity() {
        return storeProductService.findPromotionalSortedByQuantity();
    }

    // Менеджер п. 16 / Касир п. 13: Не акційні товари, відсортовані за назвою
    @GetMapping("/non-promotional/sorted-by-name")
    @PreAuthorize("hasAnyRole('MANAGER', 'CASHIER')")
    public List<StoreProductFullResponse> getNonPromotionalStoreProductsSortedByName() {
        return storeProductService.findNonPromotionalSortedByName();
    }

    // Менеджер п. 16 / Касир п. 13: Не акційні товари, відсортовані за кількістю
    @GetMapping("/non-promotional/sorted-by-quantity")
    @PreAuthorize("hasAnyRole('MANAGER', 'CASHIER')")
    public List<StoreProductFullResponse> getNonPromotionalStoreProductsSortedByQuantity() {
        return storeProductService.findNonPromotionalSortedByQuantity();
    }

    @GetMapping("/{upc}/details")
    @PreAuthorize("hasRole('MANAGER')")
    public StoreProductFullResponse getStoreProductDetailsForManager(@PathVariable String upc) {
        return storeProductService.findFullDetailsByUpc(upc);
    }

    // Касир п. 14: Тільки ціна та кількість
    @GetMapping("/{upc}")
    @PreAuthorize("hasAnyRole('CASHIER', 'MANAGER')")
    public StoreProductCashierResponse getStoreProductForCashier(@PathVariable String upc) {
        return storeProductService.findCashierInfoByUpc(upc);
    }

    // Менеджер п. 21: Загальна кількість одиниць певного товару, проданого за період
    @GetMapping("/{upc}/total-sold")
    @PreAuthorize("hasRole('MANAGER')")
    public Integer getTotalSold(
            @PathVariable String upc,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return storeProductService.getTotalSoldQuantity(upc, from, to);
    }
}