package ua.edu.ukma.zlagodabackend.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ua.edu.ukma.zlagodabackend.dto.report.BaseBasketItemResponse;
import ua.edu.ukma.zlagodabackend.dto.report.CategorySalesVolumeResponse;
import ua.edu.ukma.zlagodabackend.dto.report.CityCustomerStatsResponse;

import java.time.LocalDateTime;
import java.util.List;
import ua.edu.ukma.zlagodabackend.dto.report.LoyalCategoryFanResponse;
import ua.edu.ukma.zlagodabackend.dto.report.TopProductPremiumResponse;
import ua.edu.ukma.zlagodabackend.dto.report.VipCustomerResponse;

@Repository
@RequiredArgsConstructor
public class ComplexQueryDao {

    private final JdbcTemplate jdbc;

    public List<CategorySalesVolumeResponse> getCategorySalesVolume(LocalDateTime from, LocalDateTime to) {
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

        return jdbc.query(sql, (rs, rowNum) -> new CategorySalesVolumeResponse(
            rs.getString("category_name"),
            rs.getLong("total_sold_pieces")
        ), from, to);
    }

    public List<VipCustomerResponse> getVipCustomers() {
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

        return jdbc.query(sql, (rs, rowNum) -> new VipCustomerResponse(
            rs.getString("card_number"),
            rs.getString("cust_surname"),
            rs.getString("cust_name")
        ));
    }

    public List<LoyalCategoryFanResponse> getLoyalCategoryFans(String categoryName) {
        String sql = """
            SELECT cc.card_number, cc.cust_surname, cc.cust_name
            FROM Customer_Card cc
            WHERE NOT EXISTS (
                SELECT sp.upc
                FROM Store_Product sp
                JOIN Product p ON sp.id_product = p.id_product
                JOIN Category c ON p.category_number = c.category_number
                WHERE c.category_name = ?
                  AND NOT EXISTS (
                    SELECT 1
                    FROM "check" ch
                    JOIN Sale s ON ch.check_number = s.check_number
                    WHERE ch.card_number = cc.card_number
                      AND s.upc = sp.upc
                )
            )
            """;
            
        return jdbc.query(sql, (rs, rowNum) -> new LoyalCategoryFanResponse(
                rs.getString("card_number"),
                rs.getString("cust_surname"),
                rs.getString("cust_name")
        ), categoryName);
    }

    public List<TopProductPremiumResponse> getTopProductsPremium() {
        String sql = """
            SELECT p.product_name, SUM(s.selling_price * s.product_number) AS total_revenue
            FROM Product p
            JOIN Store_Product sp ON p.id_product = sp.id_product
            JOIN Sale s ON sp.upc = s.upc
            JOIN "check" ch ON s.check_number = ch.check_number
            JOIN Customer_Card cc ON ch.card_number = cc.card_number
            WHERE cc.percent >= 10
            GROUP BY p.id_product, p.product_name
            ORDER BY total_revenue DESC
            LIMIT 3
            """;
            
        return jdbc.query(sql, (rs, rowNum) -> new TopProductPremiumResponse(
                rs.getString("product_name"),
                rs.getBigDecimal("total_revenue")
        ));
    }

    public List<CityCustomerStatsResponse> getCustomerStatsByCity(String city) {
        String sql = """
                SELECT cc.cust_surname, cc.cust_name, COUNT(DISTINCT ch.check_number) AS checks_count,
                       SUM(s.selling_price * s.product_number) AS total_spent
                FROM Customer_Card cc
                JOIN "check" ch ON cc.card_number = ch.card_number
                JOIN Sale s ON ch.check_number = s.check_number
                WHERE cc.city = ?
                GROUP BY cc.card_number, cc.cust_surname, cc.cust_name
                ORDER BY total_spent DESC
                """;

        return jdbc.query(sql, (rs, rowNum) -> new CityCustomerStatsResponse(
                rs.getString("card_number"),
                rs.getString("cust_surname"),
                rs.getString("cust_name"),
                rs.getInt("total_checks"),
                rs.getInt("total_items_bought"),
                rs.getBigDecimal("total_spent")
        ), city);
    }

    public List<BaseBasketItemResponse> getBaseBasketProducts() {
        String sql = """
                SELECT p.id_product, p.product_name
                FROM Product p
                WHERE NOT EXISTS (
                    SELECT 1 FROM Customer_Card cc
                    WHERE NOT EXISTS (
                        SELECT 1 FROM "check" ch
                        JOIN Sale s ON s.check_number = ch.check_number
                        JOIN Store_Product sp ON s.upc = sp.upc
                        WHERE ch.card_number = cc.card_number
                          AND sp.id_product = p.id_product
                    )
                )
                """;

        return jdbc.query(sql, (rs, rowNum) -> new BaseBasketItemResponse(
                rs.getInt("id_product"),
                rs.getString("product_name")
        ));
    }
}
