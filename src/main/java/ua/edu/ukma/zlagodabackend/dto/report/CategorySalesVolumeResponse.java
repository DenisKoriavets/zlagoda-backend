package ua.edu.ukma.zlagodabackend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategorySalesVolumeResponse {
    private String categoryName;
    private Long totalSoldPieces;
}