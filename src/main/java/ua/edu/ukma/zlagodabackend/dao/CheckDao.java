package ua.edu.ukma.zlagodabackend.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ua.edu.ukma.zlagodabackend.dto.check.CheckDetailsDto;
import ua.edu.ukma.zlagodabackend.model.Check;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CheckDao {

    private final JdbcTemplate jdbcTemplate;

    public void save(Check check) {
        String sql = """
                INSERT INTO "check" (
                    check_number,
                    id_employee,
                    card_number,
                    print_date,
                    sum_total,
                    vat
                ) VALUES (?, ?, ?, ?, ?, ?)
                """;
                
        jdbcTemplate.update(sql,
                check.getCheckNumber(),
                check.getIdEmployee(),
                check.getCardNumber(),
                check.getPrintDate(),
                check.getSumTotal(),
                check.getVat());
    }

    public List<CheckDetailsDto> findAllWithFilters(String cashierId, LocalDateTime from, LocalDateTime to) {
        StringBuilder sql = new StringBuilder("""
                SELECT check_number, id_employee, card_number,
                       print_date, sum_total, vat
                FROM "check"
                WHERE 1=1
                """);

        List<Object> params = new ArrayList<>();

        if (cashierId != null && !cashierId.trim().isEmpty()) {
            sql.append(" AND id_employee = ? \n");
            params.add(cashierId);
        }

        if (from != null) {
            sql.append(" AND print_date >= ? \n");
            params.add(from);
        }

        if (to != null) {
            sql.append(" AND print_date <= ? \n");
            params.add(to);
        }

        sql.append(" ORDER BY print_date DESC");

        var mapper = createMapper();

        return jdbcTemplate.query(sql.toString(), mapper, params.toArray());
    }

    public List<CheckDetailsDto> findByCashierAndPeriod(String cashierId, LocalDateTime from, LocalDateTime to) {
        return findAllWithFilters(cashierId, from, to);
    }

    public List<CheckDetailsDto> findAllByPeriod(LocalDateTime from, LocalDateTime to) {
        return findAllWithFilters(null, from, to);
    }

    public BigDecimal getSalesSumByCashier(String cashierId, LocalDateTime from, LocalDateTime to) {
        String sql = """
                SELECT COALESCE(SUM(sum_total), 0)
                FROM "check"
                WHERE id_employee = ? AND print_date BETWEEN ? AND ?
                """;
        return jdbcTemplate.queryForObject(sql, BigDecimal.class, cashierId, from, to);
    }

    public BigDecimal getSalesSumAll(LocalDateTime from, LocalDateTime to) {
        String sql = """
                SELECT COALESCE(SUM(sum_total), 0)
                FROM "check"
                WHERE print_date BETWEEN ? AND ?
                """;
        return jdbcTemplate.queryForObject(sql, BigDecimal.class, from, to);
    }


    public Optional<CheckDetailsDto> findDetailsById(String checkNumber) {
        String sql = """
                SELECT check_number, id_employee, card_number, 
                       print_date, sum_total, vat
                FROM "check"
                WHERE check_number = ?
                """;

        var mapper = createMapper();

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, mapper, checkNumber));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public int countByEmployeeId(String idEmployee) {
        String sql = "SELECT COUNT(*) FROM \"check\" WHERE id_employee = ?";
        Integer n = jdbcTemplate.queryForObject(sql, Integer.class, idEmployee);
        return n != null ? n : 0;
    }

    public int countByCardNumber(String cardNumber) {
        String sql = "SELECT COUNT(*) FROM \"check\" WHERE card_number = ?";
        Integer n = jdbcTemplate.queryForObject(sql, Integer.class, cardNumber);
        return n != null ? n : 0;
    }

    public void deleteById(String checkNumber) {
        String sql = """
                DELETE
                FROM "check"
                WHERE check_number = ?
                """;
        jdbcTemplate.update(sql, checkNumber);
    }

    private RowMapper<CheckDetailsDto> createMapper() {
        RowMapper<CheckDetailsDto> mapper = (rs, rowNum) -> {
            CheckDetailsDto dto = new CheckDetailsDto();
            dto.setCheckNumber(rs.getString("check_number"));
            dto.setIdEmployee(rs.getString("id_employee"));
            dto.setCardNumber(rs.getString("card_number"));
            dto.setPrintDate(rs.getTimestamp("print_date").toLocalDateTime());
            dto.setSumTotal(rs.getBigDecimal("sum_total"));
            dto.setVat(rs.getBigDecimal("vat"));
            return dto;
        };
        return mapper;
    }
}