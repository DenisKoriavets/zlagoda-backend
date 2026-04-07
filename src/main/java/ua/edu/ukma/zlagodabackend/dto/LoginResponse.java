package ua.edu.ukma.zlagodabackend.dto;

public record LoginResponse(
    String token,
    String role,
    String idEmployee
) {}