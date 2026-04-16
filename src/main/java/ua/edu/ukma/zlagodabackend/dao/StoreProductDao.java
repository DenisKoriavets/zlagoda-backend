package ua.edu.ukma.zlagodabackend.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ua.edu.ukma.zlagodabackend.dto.storeProduct.StoreProductCashierResponse;
import ua.edu.ukma.zlagodabackend.dto.storeProduct.StoreProductFullResponse;
import ua.edu.ukma.zlagodabackend.model.StoreProduct;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StoreProductDao {

    private final JdbcTemplate jdbc;

    // Mapper для повної моделі менеджера
    private final RowMapper<StoreProductFullResponse> fullMapper = (rs, rowNum) -> new StoreProductFullResponse(
            rs.getString("upc"), rs.getString("upc_prom"), rs.getInt("id_product"),
            rs.getBigDecimal("selling_price"), rs.getInt("products_number"),
            rs.getBoolean("promotional_product"), rs.getString("product_name"),
            rs.getString("characteristics")
    );

    // Mapper для касира
    private final RowMapper<StoreProductCashierResponse> cashierMapper = (rs, rowNum) -> new StoreProductCashierResponse(
            rs.getString("upc"), rs.getBigDecimal("selling_price"), rs.getInt("products_number")
    );

    // Mapper для внутрішніх операцій сервісу
    private final RowMapper<StoreProduct> entityMapper = (rs, rowNum) -> new StoreProduct(
            rs.getString("upc"), rs.getString("upc_prom"), rs.getInt("id_product"),
            rs.getBigDecimal("selling_price"), rs.getInt("products_number"),
            rs.getBoolean("promotional_product")
    );

    // --- Методи для Касира ---

    // Касир п. 2: Усі товари у магазині, посортовані за назвою
    public List<StoreProductFullResponse> findAllSortedByName() {
        String sql = """
            SELECT sp.*, p.product_name, p.characteristics 
            FROM Store_Product sp JOIN Product p ON sp.id_product = p.id_product 
            ORDER BY p.product_name ASC""";
        return jdbc.query(sql, fullMapper);
    }

    // Касир п. 14: За UPC-товару знайти ціну продажу, кількість [cite: 142]
    public Optional<StoreProductCashierResponse> findCashierInfoByUpc(String upc) {
        String sql = "SELECT upc, selling_price, products_number FROM Store_Product WHERE upc = ?";
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, cashierMapper, upc));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    // --- Методи для Менеджера ---

    // Менеджер п. 10: Усі товари у магазині, посортовані за кількістю
    public List<StoreProductFullResponse> findAllSortedByQuantity() {
        String sql = """
            SELECT sp.*, p.product_name, p.characteristics 
            FROM Store_Product sp JOIN Product p ON sp.id_product = p.id_product 
            ORDER BY sp.products_number DESC""";
        return jdbc.query(sql, fullMapper);
    }

    // Менеджер п. 15 / Касир п. 12: Акційні товари [cite: 107, 138]
    public List<StoreProductFullResponse> findPromotionalSortedByName() {
        String sql = """
            SELECT sp.*, p.product_name, p.characteristics 
            FROM Store_Product sp JOIN Product p ON sp.id_product = p.id_product 
            WHERE sp.promotional_product = true ORDER BY p.product_name ASC""";
        return jdbc.query(sql, fullMapper);
    }

    public List<StoreProductFullResponse> findPromotionalSortedByQuantity() {
        String sql = """
            SELECT sp.*, p.product_name, p.characteristics 
            FROM Store_Product sp JOIN Product p ON sp.id_product = p.id_product 
            WHERE sp.promotional_product = true ORDER BY sp.products_number DESC""";
        return jdbc.query(sql, fullMapper);
    }

    // Менеджер п. 16 / Касир п. 13: Не акційні товари [cite: 108, 139]
    public List<StoreProductFullResponse> findNonPromotionalSortedByName() {
        String sql = """
            SELECT sp.*, p.product_name, p.characteristics 
            FROM Store_Product sp JOIN Product p ON sp.id_product = p.id_product 
            WHERE sp.promotional_product = false ORDER BY p.product_name ASC""";
        return jdbc.query(sql, fullMapper);
    }

    // Менеджер п. 14: Повна інформація за UPC [cite: 106]
    public Optional<StoreProductFullResponse> findFullDetailsByUpc(String upc) {
        String sql = """
            SELECT sp.*, p.product_name, p.characteristics 
            FROM Store_Product sp JOIN Product p ON sp.id_product = p.id_product 
            WHERE sp.upc = ?""";
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, fullMapper, upc));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<StoreProduct> findByProductAndPromo(Integer idProduct, boolean isPromotional) {
        String sql = "SELECT * FROM Store_Product WHERE id_product = ? AND promotional_product = ?";
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, entityMapper, idProduct, isPromotional));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void save(StoreProduct sp) {
        String sql = "INSERT INTO Store_Product (upc, upc_prom, id_product, selling_price, products_number, promotional_product) VALUES (?, ?, ?, ?, ?, ?)";
        jdbc.update(sql, sp.getUpc(), sp.getUpcProm(), sp.getIdProduct(), sp.getSellingPrice(), sp.getProductsNumber(), sp.getPromotionalProduct());
    }

    public void update(StoreProduct sp) {
        String sql = "UPDATE Store_Product SET upc_prom = ?, id_product = ?, selling_price = ?, products_number = ?, promotional_product = ? WHERE upc = ?";
        jdbc.update(sql, sp.getUpcProm(), sp.getIdProduct(), sp.getSellingPrice(), sp.getProductsNumber(), sp.getPromotionalProduct(), sp.getUpc());
    }

    public void deleteByUpc(String upc) {
        jdbc.update("DELETE FROM Store_Product WHERE upc = ?", upc);
    }

    public Optional<StoreProduct> findEntityByUpc(String upc) {
        try {
            return Optional.ofNullable(jdbc.queryForObject("SELECT * FROM Store_Product WHERE upc = ?", entityMapper, upc));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}