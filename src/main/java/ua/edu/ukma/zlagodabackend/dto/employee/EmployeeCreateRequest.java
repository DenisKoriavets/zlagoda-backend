package ua.edu.ukma.zlagodabackend.dto.employee;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeCreateRequest(
    @NotBlank(message = "ID є обов'язковим") String idEmployee,
    @NotBlank(message = "Прізвище є обов'язковим") String emplSurname,
    @NotBlank(message = "Ім'я є обов'язковим") String emplName,
    String emplPatronymic,
    @NotBlank(message = "Роль є обов'язковою") String emplRole,
    @PositiveOrZero(message = "Зарплата не може бути від'ємною") BigDecimal salary,
    @NotNull(message = "Дата народження є обов'язковою") LocalDate dateOfBirth,
    @NotNull(message = "Дата початку роботи є обов'язковою") LocalDate dateOfStart,
    @NotBlank(message = "Телефон є обов'язковим") String phoneNumber,
    @NotBlank(message = "Місто є обов'язковим") String city,
    @NotBlank(message = "Вулиця є обов'язковою") String street,
    @NotBlank(message = "Індекс є обов'язковим") String zipCode,
    @NotBlank(message = "Пароль є обов'язковим для нового працівника") String password 
) {}