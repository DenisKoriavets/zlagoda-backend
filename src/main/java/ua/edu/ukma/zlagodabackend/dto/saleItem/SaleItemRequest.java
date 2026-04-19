package ua.edu.ukma.zlagodabackend.dto.saleItem;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SaleItemRequest(
        @NotBlank(message = "UPC є обов'язковим")
        String upc,

        @NotNull(message = "Кількість є обов'язковою")
        @Min(value = 1, message = "Кількість повинна бути мінімум 1")
        Integer quantity
) {}