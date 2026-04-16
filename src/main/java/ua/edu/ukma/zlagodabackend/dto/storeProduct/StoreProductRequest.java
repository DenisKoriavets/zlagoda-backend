package ua.edu.ukma.zlagodabackend.dto.storeProduct;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record StoreProductRequest(
        @NotBlank(message = "UPC є обов'язковим")
        @Size(max = 12, message = "UPC не може перевищувати 12 символів ")
        String upc,

        @NotNull(message = "ID базового товару є обов'язковим ")
        Integer idProduct,

        @DecimalMin(value = "0.0", inclusive = false, message = "Ціна продажу повинна бути більшою за 0 ")
        @Digits(integer = 9, fraction = 4, message = "Некоректний формат ціни (до 13 знаків загалом, 4 після коми) ")
        BigDecimal sellingPrice,

        @NotNull(message = "Кількість товарів є обов'язковою")
        @Min(value = 0, message = "Кількість не може бути від'ємною ")
        Integer productsNumber,

        @NotNull(message = "Вкажіть, чи є товар акційним [cite: 34]")
        Boolean promotionalProduct
) {
}