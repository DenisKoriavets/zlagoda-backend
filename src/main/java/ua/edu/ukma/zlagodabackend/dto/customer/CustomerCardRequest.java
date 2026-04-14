package ua.edu.ukma.zlagodabackend.dto.customer;

import jakarta.validation.constraints.*;
import ua.edu.ukma.zlagodabackend.annotation.ValidPhone;

public record CustomerCardRequest(
    @NotBlank(message = "Номер карти є обов'язковим")
    @Size(max = 13, message = "Номер карти не може перевищувати 13 символів")
    String cardNumber,

    @NotBlank(message = "Прізвище є обов'язковим")
    @Size(max = 50)
    String custSurname,

    @NotBlank(message = "Ім'я є обов'язковим")
    @Size(max = 50)
    String custName,

    @Size(max = 50)
    String custPatronymic,

    @NotBlank(message = "Телефон є обов'язковим")
    @ValidPhone
    String phoneNumber,

    @Size(max = 50)
    String city,

    @Size(max = 50)
    String street,

    @Size(max = 9)
    String zipCode,

    @NotNull(message = "Відсоток знижки є обов'язковим")
    @Min(value = 0, message = "Відсоток не може бути від'ємним")
    @Max(value = 100, message = "Відсоток не може перевищувати 100")
    Integer percent
) {}
