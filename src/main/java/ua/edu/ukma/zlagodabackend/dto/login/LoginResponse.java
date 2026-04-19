package ua.edu.ukma.zlagodabackend.dto.login;

public record LoginResponse(
    String token,
    String role,
    String idEmployee
) {}