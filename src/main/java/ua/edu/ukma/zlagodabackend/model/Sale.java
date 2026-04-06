package ua.edu.ukma.zlagodabackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sale {
    private String upc;
    private String checkNumber;
    private Integer productNumber;
    private BigDecimal sellingPrice;
}