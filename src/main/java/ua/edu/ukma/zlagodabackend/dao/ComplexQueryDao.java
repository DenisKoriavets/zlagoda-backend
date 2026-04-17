package ua.edu.ukma.zlagodabackend.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ComplexQueryDao {

    private final JdbcTemplate jdbc;

    public List<Map<String, Object>> getCategorySalesVolume(LocalDateTime from, LocalDateTime to) {
        String sql = """
            SELECT c.category_name, SUM(s.product_number) AS total_sold_pieces
            FROM Category c
            JOIN Product p ON c.category_number = p.category_number
            JOIN Store_Product sp ON p.id_product = sp.id_product
            JOIN Sale s ON sp.upc = s.upc
            JOIN "check" ch ON s.check_number = ch.check_number
            WHERE ch.print_date BETWEEN ? AND ?
            GROUP BY c.category_number, c.category_name
            ORDER BY total_sold_pieces DESC
            """;
        return jdbc.queryForList(sql, from, to);
    }

    public List<Map<String, Object>> getVipCustomers() {
        String sql = """
            SELECT cc.card_number, cc.cust_surname, cc.cust_name
            FROM Customer_Card cc
            WHERE NOT EXISTS (
                SELECT c.category_number
                FROM Category c
                WHERE NOT EXISTS (
                    SELECT 1
                    FROM "check" ch
                    JOIN Sale s ON ch.check_number = s.check_number
                    JOIN Store_Product sp ON s.upc = sp.upc
                    JOIN Product p ON sp.id_product = p.id_product
                    WHERE ch.card_number = cc.card_number
                      AND p.category_number = c.category_number
                )
            )
            """;
        return jdbc.queryForList(sql);
    }

}