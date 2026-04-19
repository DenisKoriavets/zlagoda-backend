package ua.edu.ukma.zlagodabackend.dto.report;

import java.math.BigDecimal;

public record CityCustomerStatsResponse(
        String cardNumber,
        String custSurname,
        String custName,
        Integer totalChecks,
        Integer totalItemsBought,
        BigDecimal totalSpent
) {}