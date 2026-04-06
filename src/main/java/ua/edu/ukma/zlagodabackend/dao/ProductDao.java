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

    private final RowMapper<Product> rowMapper = (rs, rowNum) -> new Product(
            rs.getInt("id_product"),
            rs.getInt("category_number"),
            rs.getString("product_name"),
            rs.getString("characteristics")
    );

    public List<Product> findAllSortedByName() {
        String sql = """
                SELECT *
                FROM Product
                ORDER BY product_name ASC
                """;
        return jdbcTemplate.query(sql, rowMapper);
    }

    public List<Product> findByCategoryIdSortedByName(Integer categoryId) {
        String sql = """
                SELECT *
                FROM Product
                WHERE category_number = ?
                ORDER BY product_name ASC
                """;
        return jdbcTemplate.query(sql, rowMapper, categoryId);
    }

    public List<Product> searchByName(String namePart) {
        String sql = """
                SELECT *
                FROM Product
                WHERE product_name ILIKE ?
                ORDER BY product_name ASC
                """;
        return jdbcTemplate.query(sql, rowMapper, "%" + namePart + "%");
    }

    public Optional<Product> findById(Integer id) {
        String sql = """
                SELECT *
                FROM Product
                WHERE id_product = ?
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
                    characteristics
                ) VALUES (?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, product.getCategoryNumber());
            ps.setString(2, product.getProductName());
            ps.setString(3, product.getCharacteristics());
            return ps;
        }, keyHolder);

        if (keyHolder.getKeys() != null) {
            product.setIdProduct((Integer) keyHolder.getKeys().get("id_product"));
        }
        return product;
    }

    public void update(Product product) {
        String sql = """
                UPDATE Product
                SET category_number = ?,
                    product_name = ?,
                    characteristics = ?
                WHERE id_product = ?
                """;

        jdbcTemplate.update(sql,
                product.getCategoryNumber(),
                product.getProductName(),
                product.getCharacteristics(),
                product.getIdProduct());
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