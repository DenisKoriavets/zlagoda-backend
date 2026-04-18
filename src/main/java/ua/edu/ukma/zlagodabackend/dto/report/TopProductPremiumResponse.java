package ua.edu.ukma.zlagodabackend.dto.report;

import java.math.BigDecimal;

public record TopProductPremiumResponse(
        String productName,
        BigDecimal totalRevenue
) {}
