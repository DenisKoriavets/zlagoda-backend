package ua.edu.ukma.zlagodabackend.dto.storeProduct;

import java.math.BigDecimal;

public record StoreProductCashierResponse(
        String upc, BigDecimal sellingPrice, Integer productsNumber
) {}