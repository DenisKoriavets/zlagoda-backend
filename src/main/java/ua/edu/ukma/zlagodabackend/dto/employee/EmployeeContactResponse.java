package ua.edu.ukma.zlagodabackend.dto.employee;

public record EmployeeContactResponse(
        String idEmployee,
        String fullName,
        String phoneNumber,
        String address
) {}