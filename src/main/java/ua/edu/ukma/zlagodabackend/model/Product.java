package ua.edu.ukma.zlagodabackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    Integer idProduct;
    Integer categoryNumber;
    String productName;
    String producer;
    String characteristics;
    /** Кількість UPC у магазині для цього id_product */
    Integer storeProductCount;
    /** Заповнюється в GET за id: чи є звичайна позиція Store_Product */
    Boolean hasRegularStoreProduct;
    /** Заповнюється в GET за id: чи є акційна позиція */
    Boolean hasPromotionalStoreProduct;
}
