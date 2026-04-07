package ua.edu.ukma.zlagodabackend.dao;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ua.edu.ukma.zlagodabackend.dto.saleItem.SaleItemResponse;
import ua.edu.ukma.zlagodabackend.model.Sale;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SaleDao {

    private final JdbcTemplate jdbcTemplate;

    public void save(Sale sale) {
        String sql = """
                INSERT INTO Sale (
                    upc,
                    check_number,
                    product_number,
                    selling_price
                ) VALUES (?, ?, ?, ?)
                """;
        
        jdbcTemplate.update(sql, 
                sale.getUpc(), 
                sale.getCheckNumber(),
                sale.getProductNumber(), 
                sale.getSellingPrice());
    }

    public List<SaleItemResponse> findByCheckNumber(String checkNumber) {
        String sql = """
                SELECT s.upc, p.product_name, s.product_number, s.selling_price
                FROM Sale s
                JOIN Store_Product sp ON s.upc = sp.upc
                JOIN Product p ON sp.id_product = p.id_product
                WHERE s.check_number = ?
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new SaleItemResponse(
                rs.getString("upc"),
                rs.getString("product_name"),
                rs.getInt("product_number"),
                rs.getBigDecimal("selling_price")
        ), checkNumber);
    }

    public int getTotalQuantitySold(String upc, LocalDateTime from, LocalDateTime to) {
        String sql = """
            SELECT COALESCE(SUM(s.product_number), 0)
            FROM Sale s
            JOIN "Check" c ON s.check_number = c.check_number
            WHERE s.upc = ? AND c.print_date BETWEEN ? AND ?
            """;
        return jdbcTemplate.queryForObject(sql, Integer.class, upc, from, to);
    }
}