package ua.edu.ukma.zlagodabackend.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ua.edu.ukma.zlagodabackend.model.Sale;

import java.time.LocalDateTime;

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

    public int countRowsByUpc(String upc) {
        String sql = "SELECT COUNT(*) FROM Sale WHERE upc = ?";
        Integer n = jdbcTemplate.queryForObject(sql, Integer.class, upc);
        return n != null ? n : 0;
    }

    public int getTotalQuantitySold(String upc, LocalDateTime from, LocalDateTime to) {
        String sql = """
                SELECT COALESCE(SUM(s.product_number), 0)
                FROM Sale s
                JOIN "check" c ON s.check_number = c.check_number
                WHERE s.upc = ? AND c.print_date BETWEEN ? AND ?
                """;
        return jdbcTemplate.queryForObject(sql, Integer.class, upc, from, to);
    }
}