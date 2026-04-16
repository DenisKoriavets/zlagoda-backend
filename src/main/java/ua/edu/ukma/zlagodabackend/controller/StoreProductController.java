package ua.edu.ukma.zlagodabackend.controller;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ua.edu.ukma.zlagodabackend.dto.storeProduct.StoreProductDetailsDto;
import ua.edu.ukma.zlagodabackend.dto.storeProduct.StoreProductRequest;
import ua.edu.ukma.zlagodabackend.model.StoreProduct;
import ua.edu.ukma.zlagodabackend.service.StoreProductService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/store-products")
@RequiredArgsConstructor
public class StoreProductController {

    private final StoreProductService storeProductService;

    @GetMapping("/sorted-by-name")
    public List<StoreProductDetailsDto> getStoreProductsSortedByName() {
        return storeProductService.findAll(null, null, null, "name");
    }

    @GetMapping("/sorted-by-quantity")
    public List<StoreProductDetailsDto> getStoreProductsSortedByQuantity() {
        return storeProductService.findAll(null, null, null, "quantity");
    }

    @GetMapping("/promotional/sorted-by-name")
    public List<StoreProductDetailsDto> getPromotionalStoreProductsSortedByName() {
        return storeProductService.findAll(true, null, null, "name");
    }

    @GetMapping("/promotional/sorted-by-quantity")
    public List<StoreProductDetailsDto> getPromotionalStoreProductsSortedByQuantity() {
        return storeProductService.findAll(true, null, null, "quantity");
    }

    @GetMapping("/non-promotional/sorted-by-name")
    public List<StoreProductDetailsDto> getNonPromotionalStoreProductsSortedByName() {
        return storeProductService.findAll(false, null, null, "name");
    }

    @GetMapping("/non-promotional/sorted-by-quantity")
    public List<StoreProductDetailsDto> getNonPromotionalStoreProductsSortedByQuantity() {
        return storeProductService.findAll(false, null, null, "quantity");
    }

    @GetMapping("/search-by-name-or-upc/{query}/sorted-by-name")
    public List<StoreProductDetailsDto> searchStoreProductsByNameOrUpcSortedByName(@PathVariable String query) {
        return storeProductService.searchByNameOrUpcAllSortedByName(query);
    }

    @GetMapping("/search-by-name-or-upc/{query}/sorted-by-quantity")
    public List<StoreProductDetailsDto> searchStoreProductsByNameOrUpcSortedByQuantity(@PathVariable String query) {
        return storeProductService.searchByNameOrUpcAllSortedByQuantity(query);
    }

    @GetMapping("/promotional/search-by-name-or-upc/{query}/sorted-by-name")
    public List<StoreProductDetailsDto> searchPromotionalByNameOrUpcSortedByName(@PathVariable String query) {
        return storeProductService.searchByNameOrUpcPromotionalSortedByName(query);
    }

    @GetMapping("/promotional/search-by-name-or-upc/{query}/sorted-by-quantity")
    public List<StoreProductDetailsDto> searchPromotionalByNameOrUpcSortedByQuantity(@PathVariable String query) {
        return storeProductService.searchByNameOrUpcPromotionalSortedByQuantity(query);
    }

    @GetMapping("/non-promotional/search-by-name-or-upc/{query}/sorted-by-name")
    public List<StoreProductDetailsDto> searchNonPromotionalByNameOrUpcSortedByName(@PathVariable String query) {
        return storeProductService.searchByNameOrUpcNonPromotionalSortedByName(query);
    }

    @GetMapping("/non-promotional/search-by-name-or-upc/{query}/sorted-by-quantity")
    public List<StoreProductDetailsDto> searchNonPromotionalByNameOrUpcSortedByQuantity(@PathVariable String query) {
        return storeProductService.searchByNameOrUpcNonPromotionalSortedByQuantity(query);
    }

    @GetMapping("/{upc}/total-sold")
    @PreAuthorize("hasRole('MANAGER')")
    public Integer getTotalSold(
        @PathVariable String upc,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return storeProductService.getTotalSoldQuantity(upc, from, to);
    }

    @GetMapping("/{upc}")
    public StoreProductDetailsDto getStoreProductByUpc(@PathVariable String upc) {
        return storeProductService.findDetailsByUpc(upc);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('MANAGER')")
    public StoreProduct createStoreProduct(@Valid @RequestBody StoreProductRequest request) {
        return storeProductService.create(request);
    }

    @PutMapping("/{upc}")
    @PreAuthorize("hasRole('MANAGER')")
    public StoreProduct updateStoreProduct
            (
                    @PathVariable String upc,
                    @Valid @RequestBody StoreProductRequest request
            ) {
        return storeProductService.update(upc, request);
    }

    @DeleteMapping("/{upc}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('MANAGER')")
    public void deleteStoreProduct(@PathVariable String upc) {
        storeProductService.delete(upc);
    }
}
