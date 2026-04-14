package ua.edu.ukma.zlagodabackend.dto.storeProduct;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreProductDetailsDto {
    private String upc;
    private String upcProm;
    private Integer idProduct;
    private BigDecimal sellingPrice;
    private Integer productsNumber;
    private Boolean promotionalProduct;
    private String productName;
    private String characteristics;
    /** Рядків у Sale з цим UPC; >0 — видалення заборонене (NO ACTION) */
    private Integer saleRowsCount;
}