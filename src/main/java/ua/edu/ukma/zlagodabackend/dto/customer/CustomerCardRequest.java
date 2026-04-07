package ua.edu.ukma.zlagodabackend.dto.customer;

import jakarta.validation.constraints.*;

public record CustomerCardRequest(
    @NotBlank(message = "Номер карти є обов'язковим") String cardNumber,
    @NotBlank(message = "Прізвище є обов'язковим") String custSurname,
    @NotBlank(message = "Ім'я є обов'язковим") String custName,
    String custPatronymic,
    @NotBlank(message = "Телефон є обов'язковим") String phoneNumber,
    String city,
    String street,
    String zipCode,
    @Min(0) @Max(100) Integer percent
) {}