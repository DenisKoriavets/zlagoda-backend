package ua.edu.ukma.zlagodabackend.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ua.edu.ukma.zlagodabackend.model.CustomerCard;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CustomerCardDao {
    private final JdbcTemplate jdbc;

    private static final String SELECT_BASE = """
            SELECT cc.card_number, cc.cust_surname, cc.cust_name, cc.cust_patronymic,
                   cc.phone_number, cc.city, cc.street, cc.zip_code, cc.percent,
                   (SELECT COUNT(*)::int FROM "check" chk WHERE chk.card_number = cc.card_number) AS check_count
            FROM Customer_Card cc
            """;

    private final RowMapper<CustomerCard> mapper = (rs, rowNum) -> new CustomerCard(
        rs.getString("card_number"), rs.getString("cust_surname"), rs.getString("cust_name"),
        rs.getString("cust_patronymic"), rs.getString("phone_number"), rs.getString("city"),
        rs.getString("street"), rs.getString("zip_code"), rs.getInt("percent"),
        rs.getInt("check_count")
    );

    public List<CustomerCard> findAll() {
        return jdbc.query(SELECT_BASE + " ORDER BY cc.cust_surname", mapper);
    }

    public Optional<CustomerCard> findById(String cardNumber) {
        String sql = SELECT_BASE + " WHERE cc.card_number = ?";
        return jdbc.query(sql, mapper, cardNumber).stream().findFirst();
    }

    public List<CustomerCard> findBySurname(String surname) {
        String sql = SELECT_BASE + " WHERE cc.cust_surname ILIKE ? ORDER BY cc.cust_surname";
        return jdbc.query(sql, mapper, surname + "%");
    }

    public List<CustomerCard> findByPercent(int percent) {
        String sql = SELECT_BASE + " WHERE cc.percent = ? ORDER BY cc.cust_surname";
        return jdbc.query(sql, mapper, percent);
    }

    public void save(CustomerCard c) {
        String sql = "INSERT INTO Customer_Card VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbc.update(sql, c.getCardNumber(), c.getCustSurname(), c.getCustName(), c.getCustPatronymic(),
                c.getPhoneNumber(), c.getCity(), c.getStreet(), c.getZipCode(), c.getPercent());
    }

    public void update(CustomerCard c) {
        String sql = """
                UPDATE Customer_Card SET cust_surname=?, cust_name=?, cust_patronymic=?,
                phone_number=?, city=?, street=?, zip_code=?, percent=? WHERE card_number=?
                """;
        jdbc.update(sql, c.getCustSurname(), c.getCustName(), c.getCustPatronymic(),
                c.getPhoneNumber(), c.getCity(), c.getStreet(), c.getZipCode(), c.getPercent(), c.getCardNumber());
    }

    public void delete(String cardNumber) {
        jdbc.update("DELETE FROM Customer_Card WHERE card_number = ?", cardNumber);
    }
}
