package ua.edu.ukma.zlagodabackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCard {
    private String cardNumber;
    private String custSurname;
    private String custName;
    private String custPatronymic;
    private String phoneNumber;
    private String city;
    private String street;
    private String zipCode;
    private Integer percent;
    /** Кількість чеків з цією карткою (для перевірки перед видаленням) */
    private Integer checkCount;
}