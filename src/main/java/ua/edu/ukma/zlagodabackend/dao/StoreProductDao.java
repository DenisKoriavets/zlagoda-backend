package ua.edu.ukma.zlagodabackend.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ua.edu.ukma.zlagodabackend.dto.StoreProductDetailsDto;
import ua.edu.ukma.zlagodabackend.model.StoreProduct;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StoreProductDao {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<StoreProduct> entityRowMapper = (rs, rowNum) -> new StoreProduct(
            rs.getString("upc"),
            rs.getString("upc_prom"),
            rs.getInt("id_product"),
            rs.getBigDecimal("selling_price"),
            rs.getInt("products_number"),
            rs.getBoolean("promotional_product")
    );

    private final RowMapper<StoreProductDetailsDto> detailsRowMapper = (rs, rowNum) -> new StoreProductDetailsDto(
            rs.getString("upc"),
            rs.getString("upc_prom"),
            rs.getInt("id_product"),
            rs.getBigDecimal("selling_price"),
            rs.getInt("products_number"),
            rs.getBoolean("promotional_product"),
            rs.getString("product_name"),
            rs.getString("characteristics")
    );

    public List<StoreProductDetailsDto> findAllWithFilters(Boolean isPromotional, String sortBy) {
        StringBuilder sql = new StringBuilder("""
                SELECT sp.*, p.product_name, p.characteristics
                FROM Store_Product sp
                JOIN Product p ON sp.id_product = p.id_product
                """);

        List<Object> params = new ArrayList<>();

        if (isPromotional != null) {
            sql.append(" WHERE sp.promotional_product = ? \n");
            params.add(isPromotional);
        }

        if ("quantity".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY sp.products_number DESC");
        } else {
            sql.append(" ORDER BY p.product_name ASC");
        }

        return jdbcTemplate.query(sql.toString(), detailsRowMapper, params.toArray());
    }

    public Optional<StoreProductDetailsDto> findDetailsByUpc(String upc) {
        String sql = """
                SELECT sp.*, p.product_name, p.characteristics
                FROM Store_Product sp
                JOIN Product p ON sp.id_product = p.id_product
                WHERE sp.upc = ?
                """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, detailsRowMapper, upc));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<StoreProduct> findEntityByUpc(String upc) {
        String sql = "SELECT * FROM Store_Product WHERE upc = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, entityRowMapper, upc));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void save(StoreProduct sp) {
        String sql = """
                INSERT INTO Store_Product (upc, upc_prom, id_product, selling_price, products_number, promotional_product)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        jdbcTemplate.update(sql, sp.getUpc(), sp.getUpcProm(), sp.getIdProduct(),
                sp.getSellingPrice(), sp.getProductsNumber(), sp.getPromotionalProduct());
    }

    public void update(StoreProduct sp) {
        String sql = """
                UPDATE Store_Product
                SET upc_prom = ?, id_product = ?, selling_price = ?, products_number = ?, promotional_product = ?
                WHERE upc = ?
                """;
        jdbcTemplate.update(sql, sp.getUpcProm(), sp.getIdProduct(), sp.getSellingPrice(),
                sp.getProductsNumber(), sp.getPromotionalProduct(), sp.getUpc());
    }

    public void updatePriceForProduct(Integer idProduct, BigDecimal newPrice, boolean isPromotional) {
        String sql = """
                UPDATE Store_Product
                SET selling_price = ?
                WHERE id_product = ? AND promotional_product = ?
                """;
        jdbcTemplate.update(sql, newPrice, idProduct, isPromotional);
    }

    public void deleteByUpc(String upc) {
        String sql = "DELETE FROM Store_Product WHERE upc = ?";
        jdbcTemplate.update(sql, upc);
    }
}