package ua.edu.ukma.zlagodabackend.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import ua.edu.ukma.zlagodabackend.model.Employee;

@Repository
public class EmployeeDao {

    private final JdbcTemplate jdbc;

    private static final String CHECK_COUNT = """
            (SELECT COUNT(*)::int FROM "check" c WHERE c.id_employee = e.id_employee) AS check_count
            """;

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
        rs.getString("password"),
        (Integer) rs.getObject("check_count")
    );

    public Optional<Employee> findById(String id) {
        String sql = """
                SELECT e.*,
                """ + CHECK_COUNT + """
                FROM Employee e WHERE e.id_employee = ?
                """;
        return jdbc.query(sql, EMPLOYEE_MAPPER, id).stream().findFirst();
    }

    public List<Employee> findAll() {
        String sql = """
                SELECT e.*,
                """ + CHECK_COUNT + """
                FROM Employee e ORDER BY e.empl_surname
                """;
        return jdbc.query(sql, EMPLOYEE_MAPPER);
    }

    public List<Employee> findCashiers() {
        String sql = """
                SELECT e.*,
                """ + CHECK_COUNT + """
                FROM Employee e WHERE e.empl_role = 'cashier' ORDER BY e.empl_surname
                """;
        return jdbc.query(sql, EMPLOYEE_MAPPER);
    }

    public List<Employee> findManagers() {
        String sql = """
                SELECT e.*,
                """ + CHECK_COUNT + """
                FROM Employee e WHERE e.empl_role = 'manager' ORDER BY e.empl_surname
                """;
        return jdbc.query(sql, EMPLOYEE_MAPPER);
    }

    public List<Employee> findBySurname(String surname) {
        String sql = """
                SELECT e.*,
                """ + CHECK_COUNT + """
                FROM Employee e WHERE e.empl_surname LIKE ? ORDER BY e.empl_surname
                """;
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
}
