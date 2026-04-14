package ua.edu.ukma.zlagodabackend.dto.employee;

import jakarta.validation.constraints.*;
import ua.edu.ukma.zlagodabackend.annotation.ValidPhone;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeUpdateRequest(
    @NotBlank(message = "Прізвище є обов'язковим")
    @Size(max = 50, message = "Прізвище не може перевищувати 50 символів")
    String emplSurname,

    @NotBlank(message = "Ім'я є обов'язковим")
    @Size(max = 50, message = "Ім'я не може перевищувати 50 символів")
    String emplName,

    @Size(max = 50, message = "По батькові не може перевищувати 50 символів")
    String emplPatronymic,

    @NotBlank(message = "Роль є обов'язковою")
    @Pattern(regexp = "manager|cashier", message = "Роль має бути 'manager' або 'cashier'")
    String emplRole,

    @NotNull(message = "Зарплата є обов'язковою")
    @PositiveOrZero(message = "Зарплата не може бути від'ємною")
    BigDecimal salary,

    @NotNull(message = "Дата народження є обов'язковою")
    LocalDate dateOfBirth,

    @NotNull(message = "Дата початку роботи є обов'язковою")
    LocalDate dateOfStart,

    @NotBlank(message = "Телефон є обов'язковим")
    @ValidPhone
    String phoneNumber,

    @NotBlank(message = "Місто є обов'язковим")
    @Size(max = 50)
    String city,

    @NotBlank(message = "Вулиця є обов'язковою")
    @Size(max = 50)
    String street,

    @NotBlank(message = "Індекс є обов'язковим")
    @Size(max = 9)
    String zipCode
) {}
