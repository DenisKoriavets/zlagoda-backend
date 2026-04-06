package ua.edu.ukma.zlagodabackend.dto.storeProduct;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record StoreProductRequest(
        @NotBlank(message = "UPC є обов'язковим")
        @Size(max = 12, message = "UPC не може перевищувати 12 символів")
        String upc,

        String upcProm,

        @NotNull(message = "ID товару є обов'язковим")
        Integer idProduct,

        @NotNull(message = "Ціна є обов'язковою")
        @DecimalMin(value = "0.01", message = "Ціна має бути більшою за 0")
        BigDecimal sellingPrice,

        @NotNull(message = "Кількість є обов'язковою")
        @Min(value = 0, message = "Кількість не може бути від'ємною")
        Integer productsNumber,

        @NotNull(message = "Статус акції є обов'язковим")
        Boolean promotionalProduct
) {
}