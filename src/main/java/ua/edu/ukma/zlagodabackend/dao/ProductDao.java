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

    private final RowMapper<Product> rowMapper = (rs, rowNum) -> {
        Product p = new Product();
        p.setIdProduct(rs.getInt("id_product"));
        p.setCategoryNumber(rs.getInt("category_number"));
        p.setProductName(rs.getString("product_name"));
        p.setProducer(rs.getString("producer"));
        p.setCharacteristics(rs.getString("characteristics"));
        return p;
    };

    // Менеджер п. 9 / Касир п. 1: Отримати усі товари, відсортовані за назвою
    public List<Product> findAllSortedByName() {
        String sql = "SELECT * FROM Product ORDER BY product_name ASC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    // Менеджер п. 13 / Касир п. 5: Товари певної категорії, відсортовані за назвою
    public List<Product> findByCategoryIdSortedByName(Integer categoryId) {
        String sql = "SELECT * FROM Product WHERE category_number = ? ORDER BY product_name ASC";
        return jdbcTemplate.query(sql, rowMapper, categoryId);
    }

    // Касир п. 4: Пошук товарів за назвою
    public List<Product> searchByName(String namePart) {
        String sql = "SELECT * FROM Product WHERE product_name ILIKE ? ORDER BY product_name ASC";
        return jdbcTemplate.query(sql, rowMapper, "%" + namePart + "%");
    }

    public Optional<Product> findById(Integer id) {
        String sql = "SELECT * FROM Product WHERE id_product = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    // Менеджер п. 1: Введення відомостей про новий товар
    public Product save(Product product) {
        String sql = "INSERT INTO Product (category_number, product_name, producer, characteristics) VALUES (?, ?, ?, ?)";
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
        return product;
    }

    // Менеджер п. 2: Редагувати дані про товари
    public void update(Product product) {
        String sql = "UPDATE Product SET category_number = ?, product_name = ?, producer = ?, characteristics = ? WHERE id_product = ?";
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

    // Менеджер п. 3: Вилучити відомості про товар
    public void deleteById(Integer id) {
        String sql = "DELETE FROM Product WHERE id_product = ?";
        jdbcTemplate.update(sql, id);
    }
}