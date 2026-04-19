package ua.edu.ukma.zlagodabackend.dto.employee;

import java.math.BigDecimal;

public record CashierSalesResponse(
    String idEmployee,
    String emplSurname,
    String emplName,
    BigDecimal totalSum
) {}