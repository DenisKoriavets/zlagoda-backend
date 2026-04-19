package ua.edu.ukma.zlagodabackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Check {
    private String checkNumber;
    private String idEmployee;
    private String cardNumber;
    private LocalDateTime printDate;
    private BigDecimal sumTotal;
    private BigDecimal vat;
}