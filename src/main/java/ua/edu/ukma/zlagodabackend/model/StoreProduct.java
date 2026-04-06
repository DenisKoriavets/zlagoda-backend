package ua.edu.ukma.zlagodabackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreProduct {
    private String upc;
    private String upcProm;
    private Integer idProduct;
    private BigDecimal sellingPrice;
    private Integer productsNumber;
    private Boolean promotionalProduct;
}