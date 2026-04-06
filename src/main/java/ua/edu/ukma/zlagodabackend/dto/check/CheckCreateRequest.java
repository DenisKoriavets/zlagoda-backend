package ua.edu.ukma.zlagodabackend.dto.check;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import ua.edu.ukma.zlagodabackend.dto.saleItem.SaleItemRequest;

import java.util.List;

public record CheckCreateRequest(
        String cardNumber,

        @NotEmpty(message = "Чек не може бути порожнім")
        @Valid
        List<SaleItemRequest> items
) {}