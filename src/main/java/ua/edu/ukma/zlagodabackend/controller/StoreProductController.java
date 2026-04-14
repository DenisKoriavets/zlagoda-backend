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

    @GetMapping
    public List<StoreProductDetailsDto> getStoreProducts
            (
                    @RequestParam(required = false) Boolean promotional,
                    @RequestParam(required = false) Integer category,
                    @RequestParam(required = false) String search,
                    @RequestParam(defaultValue = "name") String sort
            ) {
        return storeProductService.findAll(promotional, category, search, sort);
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

    @GetMapping("/{upc}/total-sold")
    @PreAuthorize("hasRole('MANAGER')")
    public Integer getTotalSold(
        @PathVariable String upc,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return storeProductService.getTotalSoldQuantity(upc, from, to);
    }
}