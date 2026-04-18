package ua.edu.ukma.zlagodabackend.dto.report;

public record VipCustomerResponse(
    String cardNumber,
    String custSurname,
    String custName
) {}