package ua.edu.ukma.zlagodabackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    private Integer categoryNumber;
    private String categoryName;
    /** Кількість товарів у каталозі (для перевірки перед видаленням категорії) */
    private Integer productCount;
}