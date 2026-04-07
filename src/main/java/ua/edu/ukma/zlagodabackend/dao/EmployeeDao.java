package ua.edu.ukma.zlagodabackend.dao;

import java.time.LocalDateTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import ua.edu.ukma.zlagodabackend.dto.employee.CashierSalesResponse;
import ua.edu.ukma.zlagodabackend.model.Employee;

@Repository
public class EmployeeDao {

    private final JdbcTemplate jdbc;

    public EmployeeDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<Employee> EMPLOYEE_MAPPER = (rs, rowNum) -> new Employee(
        rs.getString("id_employee"),
        rs.getString("empl_surname"),
        rs.getString("empl_name"),
        rs.getString("empl_patronymic"),
        rs.getString("empl_role"),
        rs.getBigDecimal("salary"),
        rs.getDate("date_of_birth").toLocalDate(),
        rs.getDate("date_of_start").toLocalDate(),
        rs.getString("phone_number"),
        rs.getString("city"),
        rs.getString("street"),
        rs.getString("zip_code"),
        rs.getString("password")
    );

    public Optional<Employee> findById(String id) {
        String sql = "SELECT * FROM Employee WHERE id_employee = ?";
        return jdbc.query(sql, EMPLOYEE_MAPPER, id).stream().findFirst();
    }

    public List<Employee> findAll() {
        String sql = "SELECT * FROM Employee ORDER BY empl_surname";
        return jdbc.query(sql, EMPLOYEE_MAPPER);
    }

    public List<Employee> findCashiers() {
        String sql = "SELECT * FROM Employee WHERE empl_role = 'cashier' ORDER BY empl_surname";
        return jdbc.query(sql, EMPLOYEE_MAPPER);
    }

    public List<Employee> findBySurname(String surname) {
        String sql = "SELECT * FROM Employee WHERE empl_surname LIKE ? ORDER BY empl_surname";
        return jdbc.query(sql, EMPLOYEE_MAPPER, surname + "%");
    }

    public void save(Employee e) {
        String sql = "INSERT INTO Employee (id_employee, empl_surname, empl_name, empl_patronymic, " +
            "empl_role, salary, date_of_birth, date_of_start, phone_number, city, street, zip_code, password) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbc.update(sql, e.getIdEmployee(), e.getEmplSurname(), e.getEmplName(), e.getEmplPatronymic(),
            e.getEmplRole(), e.getSalary(), e.getDateOfBirth(), e.getDateOfStart(), e.getPhoneNumber(),
            e.getCity(), e.getStreet(), e.getZipCode(), e.getPassword());
    }

    public void update(Employee e) {
        String sql = """
            UPDATE Employee 
            SET empl_surname = ?, empl_name = ?, empl_patronymic = ?, 
                empl_role = ?, salary = ?, date_of_birth = ?, 
                date_of_start = ?, phone_number = ?, city = ?, 
                street = ?, zip_code = ?
            WHERE id_employee = ?
            """;
        jdbc.update(sql,
            e.getEmplSurname(), e.getEmplName(), e.getEmplPatronymic(),
            e.getEmplRole(), e.getSalary(), e.getDateOfBirth(),
            e.getDateOfStart(), e.getPhoneNumber(), e.getCity(),
            e.getStreet(), e.getZipCode(), e.getIdEmployee()
        );
    }

    public void delete(String id) {
        String sql = "DELETE FROM Employee WHERE id_employee = ?";
        jdbc.update(sql, id);
    }

    public Optional<CashierSalesResponse> getTotalSalesByCashier(String id, LocalDateTime from, LocalDateTime to) {
        String sql = """
            SELECT e.id_employee, e.empl_surname, e.empl_name, SUM(c.sum_total) as total_sum
            FROM Employee e
            JOIN "Check" c ON e.id_employee = c.id_employee
            WHERE e.id_employee = ? AND c.print_date BETWEEN ? AND ?
            GROUP BY e.id_employee, e.empl_surname, e.empl_name
            """;
        try {
            return jdbc.query(sql, (rs, rowNum) -> new CashierSalesResponse(
                rs.getString("id_employee"),
                rs.getString("empl_surname"),
                rs.getString("empl_name"),
                rs.getBigDecimal("total_sum")
            ), id, from, to).stream().findFirst();
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}