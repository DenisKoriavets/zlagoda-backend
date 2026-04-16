package ua.edu.ukma.zlagodabackend.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ua.edu.ukma.zlagodabackend.dto.storeProduct.StoreProductDetailsDto;
import ua.edu.ukma.zlagodabackend.exception.InsufficientStockException;
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

    private static final String SALE_COUNT = """
            (SELECT COUNT(*)::int FROM Sale s WHERE s.upc = sp.upc) AS sale_rows_count
            """;

    private final RowMapper<StoreProductDetailsDto> detailsRowMapper = (rs, rowNum) -> new StoreProductDetailsDto(
            rs.getString("upc"),
            rs.getString("upc_prom"),
            rs.getInt("id_product"),
            rs.getBigDecimal("selling_price"),
            rs.getInt("products_number"),
            rs.getBoolean("promotional_product"),
            rs.getString("product_name"),
            rs.getString("characteristics"),
            (Integer) rs.getObject("sale_rows_count")
    );

    public List<StoreProductDetailsDto> findAllWithFilters(Boolean isPromotional, Integer category, String search, String sortBy) {
        StringBuilder sql = new StringBuilder("""
                SELECT sp.upc, sp.upc_prom, sp.id_product, sp.selling_price, sp.products_number, sp.promotional_product,
                       p.product_name, p.characteristics,
                """ + SALE_COUNT + """
                FROM Store_Product sp
                JOIN Product p ON sp.id_product = p.id_product
                """);

        List<Object> params = new ArrayList<>();
        List<String> conditions = new ArrayList<>();

        if (isPromotional != null) {
            conditions.add("sp.promotional_product = ?");
            params.add(isPromotional);
        }
        if (category != null) {
            conditions.add("p.category_number = ?");
            params.add(category);
        }
        if (search != null && !search.isBlank()) {
            conditions.add("(p.product_name ILIKE ? OR sp.upc ILIKE ?)");
            String pattern = "%" + search.trim() + "%";
            params.add(pattern);
            params.add(pattern);
        }
        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }

        if ("quantity".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY sp.products_number DESC");
        } else if ("upc".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY sp.upc ASC");
        } else {
            sql.append(" ORDER BY p.product_name ASC");
        }

        return jdbcTemplate.query(sql.toString(), detailsRowMapper, params.toArray());
    }

    public Optional<StoreProductDetailsDto> findDetailsByUpc(String upc) {
        String sql = """
                SELECT sp.upc, sp.upc_prom, sp.id_product, sp.selling_price, sp.products_number, sp.promotional_product,
                       p.product_name, p.characteristics,
                """ + SALE_COUNT + """
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

    public Optional<StoreProduct> findByProductAndPromo(Integer idProduct, boolean isPromotional) {
        String sql = "SELECT * FROM Store_Product WHERE id_product = ? AND promotional_product = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, entityRowMapper, idProduct, isPromotional));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<StoreProduct> findRegularLinkedToPromo(String promoUpc) {
        String sql = "SELECT * FROM Store_Product WHERE upc_prom = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, entityRowMapper, promoUpc));
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

    public int countByIdProduct(Integer idProduct) {
        String sql = "SELECT COUNT(*) FROM Store_Product WHERE id_product = ?";
        Integer n = jdbcTemplate.queryForObject(sql, Integer.class, idProduct);
        return n != null ? n : 0;
    }

    public void deleteByUpc(String upc) {
        String sql = "DELETE FROM Store_Product WHERE upc = ?";
        jdbcTemplate.update(sql, upc);
    }

    public void decreaseQuantity(String upc, int quantityToSubtract) {
        String sql = """
                UPDATE Store_Product
                SET products_number = products_number - ?
                WHERE upc = ? AND products_number >= ?
                """;

        int updatedRows = jdbcTemplate.update(sql, quantityToSubtract, upc, quantityToSubtract);

        if (updatedRows == 0) {
            throw new InsufficientStockException("Недостатньо товару на складі для UPC " + upc);
        }
    }
}