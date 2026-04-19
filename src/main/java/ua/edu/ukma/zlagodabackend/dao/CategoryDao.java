package ua.edu.ukma.zlagodabackend.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ua.edu.ukma.zlagodabackend.model.Category;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CategoryDao {

    private final JdbcTemplate jdbcTemplate;

    private static final String COUNT_SUB = """
            (SELECT COUNT(*)::int FROM Product p WHERE p.category_number = c.category_number) AS product_count
            """;

    private final RowMapper<Category> rowMapper = (rs, rowNum) -> new Category(
            rs.getInt("category_number"),
            rs.getString("category_name"),
            (Integer) rs.getObject("product_count")
    );

    public List<Category> findAllSortedByName() {
        String sql = """
                SELECT c.category_number, c.category_name,
                """ + COUNT_SUB + """
                FROM Category c
                ORDER BY c.category_name ASC
                """;
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<Category> findById(Integer id) {
        String sql = """
                SELECT c.category_number, c.category_name,
                """ + COUNT_SUB + """
                FROM Category c
                WHERE c.category_number = ?
                """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Category save(Category category) {
        String sql = """
                INSERT INTO Category (
                    category_name
                ) VALUES (?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, category.getCategoryName());
            return ps;
        }, keyHolder);

        if (keyHolder.getKeys() != null) {
            category.setCategoryNumber((Integer) keyHolder.getKeys().get("category_number"));
        }
        category.setProductCount(0);
        return category;
    }

    public void update(Category category) {
        String sql = """
                UPDATE Category
                SET category_name = ?
                WHERE category_number = ?
                """;
        jdbcTemplate.update(sql, category.getCategoryName(), category.getCategoryNumber());
    }

    public void deleteById(Integer id) {
        String sql = """
                DELETE
                FROM Category
                WHERE category_number = ?
                """;

        jdbcTemplate.update(sql, id);
    }
}
