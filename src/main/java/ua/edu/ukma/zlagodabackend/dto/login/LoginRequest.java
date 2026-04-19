package ua.edu.ukma.zlagodabackend.dto.login;

public record LoginRequest(
    String idEmployee,
    String password
) {}