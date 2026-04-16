package ua.edu.ukma.zlagodabackend.dto.storeProduct;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreProductCashierResponse {
    private String upc;
    private BigDecimal sellingPrice;
    private Integer productsNumber;
}