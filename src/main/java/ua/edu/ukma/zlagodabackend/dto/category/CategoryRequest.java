package ua.edu.ukma.zlagodabackend.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record CategoryRequest(
        @NotBlank(message = "Назва категорії не може бути порожньою")
        @Size(max = 50, message = "Назва категорії не повинна перевищувати 50 символів")
        String categoryName
) {
}