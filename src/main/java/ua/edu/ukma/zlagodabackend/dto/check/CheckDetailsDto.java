package ua.edu.ukma.zlagodabackend.dto.check;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.edu.ukma.zlagodabackend.dto.saleItem.SaleItemResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckDetailsDto {
    private String checkNumber;
    private String idEmployee;
    private String cardNumber;
    private LocalDateTime printDate;
    private BigDecimal sumTotal;
    private BigDecimal vat;
    private List<SaleItemResponse> items;
}