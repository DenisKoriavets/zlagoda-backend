package ua.edu.ukma.zlagodabackend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.ukma.zlagodabackend.dao.ComplexQueryDao;
import ua.edu.ukma.zlagodabackend.dto.report.BaseBasketItemResponse;
import ua.edu.ukma.zlagodabackend.dto.report.CategorySalesVolumeResponse;
import ua.edu.ukma.zlagodabackend.dto.report.CityCustomerStatsResponse;
import ua.edu.ukma.zlagodabackend.dto.report.VipCustomerResponse;
import ua.edu.ukma.zlagodabackend.exception.BusinessValidationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ComplexQueryService {

    private final ComplexQueryDao complexQueryDao;

    @Transactional(readOnly = true)
    public List<CategorySalesVolumeResponse> getCategorySalesVolume(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) {
            throw new BusinessValidationException("Період (from та to) є обов'язковим.");
        }
        if (from.isAfter(to)) {
            throw new BusinessValidationException("Початкова дата не може бути пізнішою за кінцеву.");
        }
        return complexQueryDao.getCategorySalesVolume(from, to);
    }

    @Transactional(readOnly = true)
    public List<VipCustomerResponse> getVipCustomers() {
        return complexQueryDao.getVipCustomers();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getLoyalCategoryFans(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) {
            throw new BusinessValidationException("Назва категорії є обов'язковою для цього звіту.");
        }
        return complexQueryDao.getLoyalCategoryFans(categoryName.trim());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTopProductsPremium() {
        return complexQueryDao.getTopProductsPremium();
    }

    @Transactional(readOnly = true)
    public List<CityCustomerStatsResponse> getPurchasingPowerByCity(String city) {
        return complexQueryDao.getCustomerStatsByCity(city);
    }

    @Transactional(readOnly = true)
    public List<BaseBasketItemResponse> getBaseBasketProducts() {
        return complexQueryDao.getBaseBasketProducts();
    }
}
