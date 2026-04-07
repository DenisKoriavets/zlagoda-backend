package ua.edu.ukma.zlagodabackend.dto;

public record LoginRequest(
    String idEmployee,
    String password
) {}