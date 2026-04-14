package ua.edu.ukma.zlagodabackend.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ua.edu.ukma.zlagodabackend.model.Product;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductDao {

    private final JdbcTemplate jdbcTemplate;

    private static final String STORE_COUNT = """
            (SELECT COUNT(*)::int FROM Store_Product sp WHERE sp.id_product = p.id_product) AS store_product_count
            """;

    private final RowMapper<Product> rowMapper = (rs, rowNum) -> {
        Product p = new Product();
        p.setIdProduct(rs.getInt("id_product"));
        p.setCategoryNumber(rs.getInt("category_number"));
        p.setProductName(rs.getString("product_name"));
        p.setProducer(rs.getString("producer"));
        p.setCharacteristics(rs.getString("characteristics"));
        p.setStoreProductCount((Integer) rs.getObject("store_product_count"));
        return p;
    };

    public List<Product> findAllSortedByName() {
        String sql = """
                SELECT p.id_product, p.category_number, p.product_name, p.producer, p.characteristics,
                """ + STORE_COUNT + """
                FROM Product p
                ORDER BY p.product_name ASC
                """;
        return jdbcTemplate.query(sql, rowMapper);
    }

    public List<Product> findByCategoryIdSortedByName(Integer categoryId) {
        String sql = """
                SELECT p.id_product, p.category_number, p.product_name, p.producer, p.characteristics,
                """ + STORE_COUNT + """
                FROM Product p
                WHERE p.category_number = ?
                ORDER BY p.product_name ASC
                """;
        return jdbcTemplate.query(sql, rowMapper, categoryId);
    }

    public List<Product> searchByName(String namePart) {
        String sql = """
                SELECT p.id_product, p.category_number, p.product_name, p.producer, p.characteristics,
                """ + STORE_COUNT + """
                FROM Product p
                WHERE p.product_name ILIKE ?
                ORDER BY p.product_name ASC
                """;
        return jdbcTemplate.query(sql, rowMapper, "%" + namePart + "%");
    }

    public Optional<Product> findById(Integer id) {
        String sql = """
                SELECT p.id_product, p.category_number, p.product_name, p.producer, p.characteristics,
                """ + STORE_COUNT + """
                FROM Product p
                WHERE p.id_product = ?
                """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Product save(Product product) {
        String sql = """
                INSERT INTO Product (
                    category_number,
                    product_name,
                    producer,
                    characteristics
                ) VALUES (?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, product.getCategoryNumber());
            ps.setString(2, product.getProductName());
            ps.setString(3, product.getProducer());
            ps.setString(4, product.getCharacteristics());
            return ps;
        }, keyHolder);

        if (keyHolder.getKeys() != null) {
            product.setIdProduct((Integer) keyHolder.getKeys().get("id_product"));
        }
        product.setStoreProductCount(0);
        return product;
    }

    public void update(Product product) {
        String sql = """
                UPDATE Product
                SET category_number = ?,
                    product_name = ?,
                    producer = ?,
                    characteristics = ?
                WHERE id_product = ?
                """;

        jdbcTemplate.update(sql,
                product.getCategoryNumber(),
                product.getProductName(),
                product.getProducer(),
                product.getCharacteristics(),
                product.getIdProduct());
    }

    public int countByCategoryNumber(Integer categoryNumber) {
        String sql = "SELECT COUNT(*) FROM Product WHERE category_number = ?";
        Integer n = jdbcTemplate.queryForObject(sql, Integer.class, categoryNumber);
        return n != null ? n : 0;
    }

    public void deleteById(Integer id) {
        String sql = """
                DELETE
                FROM Product
                WHERE id_product = ?
                """;

        jdbcTemplate.update(sql, id);
    }
}
