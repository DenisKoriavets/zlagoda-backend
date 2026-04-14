package ua.edu.ukma.zlagodabackend.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductRequest(
        @NotNull(message = "Номер категорії є обов'язковим")
        Integer categoryNumber,

        @NotBlank(message = "Назва товару не може бути порожньою")
        @Size(max = 50, message = "Назва товару не повинна перевищувати 50 символів")
        String productName,

        @NotBlank(message = "Виробник не може бути порожнім")
        @Size(max = 50, message = "Назва виробника не повинна перевищувати 50 символів")
        String producer,

        @NotBlank(message = "Характеристики не можуть бути порожніми")
        @Size(max = 100, message = "Характеристики не повинні перевищувати 100 символів")
        String characteristics
) {
}
