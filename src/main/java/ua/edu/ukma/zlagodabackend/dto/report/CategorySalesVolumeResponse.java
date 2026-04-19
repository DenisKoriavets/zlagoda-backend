package ua.edu.ukma.zlagodabackend.dto.report;

public record CategorySalesVolumeResponse(
    String categoryName,
    Long totalSoldPieces
) {}