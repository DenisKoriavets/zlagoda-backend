package ua.edu.ukma.zlagodabackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    private String idEmployee;
    private String emplSurname;
    private String emplName;
    private String emplPatronymic;
    private String emplRole;
    private BigDecimal salary;
    private LocalDate dateOfBirth;
    private LocalDate dateOfStart;
    private String phoneNumber;
    private String city;
    private String street;
    private String zipCode;
    private String password;
}