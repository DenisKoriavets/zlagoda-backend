package ua.edu.ukma.zlagodabackend.dto.saleItem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaleItemResponse {
    private String upc;
    private String productName;
    private Integer quantity;
    private BigDecimal sellingPrice;
}