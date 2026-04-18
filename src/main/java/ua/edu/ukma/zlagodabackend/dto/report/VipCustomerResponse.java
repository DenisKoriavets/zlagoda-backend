package ua.edu.ukma.zlagodabackend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VipCustomerResponse {
    private String cardNumber;
    private String custSurname;
    private String custName;
}