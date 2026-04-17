package ua.edu.ukma.zlagodabackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ua.edu.ukma.zlagodabackend.dto.report.BaseBasketItemResponse;
import ua.edu.ukma.zlagodabackend.dto.report.CityCustomerStatsResponse;
import ua.edu.ukma.zlagodabackend.service.ComplexQueryService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class ComplexQueryController {

    private final ComplexQueryService complexQueryService;

    @GetMapping("/category-sales-volume")
    @PreAuthorize("hasRole('MANAGER')")
    public List<Map<String, Object>> getCategorySalesVolume(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return complexQueryService.getCategorySalesVolume(from, to);
    }

    @GetMapping("/vip-customers")
    @PreAuthorize("hasRole('MANAGER')")
    public List<Map<String, Object>> getVipCustomers() {
        return complexQueryService.getVipCustomers();
    }
    
    @GetMapping("/loyal-category-fans")
    @PreAuthorize("hasRole('MANAGER')")
    public List<Map<String, Object>> getLoyalCategoryFans(@RequestParam String categoryName) {
        return complexQueryService.getLoyalCategoryFans(categoryName);
    }
    
    @GetMapping("/top-products-premium")
    @PreAuthorize("hasRole('MANAGER')")
    public List<Map<String, Object>> getTopProductsPremium() {
        return complexQueryService.getTopProductsPremium();
    }

    @GetMapping("/reports/purchasing-power")
    @PreAuthorize("hasRole('MANAGER')")
    public List<CityCustomerStatsResponse> getPurchasingPowerByCity(@RequestParam String city) {
        return complexQueryService.getPurchasingPowerByCity(city);
    }

    @GetMapping("/base-basket")
    @PreAuthorize("hasRole('MANAGER')")
    public List<BaseBasketItemResponse> getBaseBasket() {
        return complexQueryService.getBaseBasketProducts();
    }
}
