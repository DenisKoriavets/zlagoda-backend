package ua.edu.ukma.zlagodabackend.dto.storeProduct;

import java.math.BigDecimal;

public record StoreProductFullResponse(
    String upc, String upcProm, Integer idProduct, BigDecimal sellingPrice,
    Integer productsNumber, Boolean promotionalProduct, String productName, String characteristics
) {}
