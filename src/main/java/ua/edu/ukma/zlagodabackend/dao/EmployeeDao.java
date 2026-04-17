package ua.edu.ukma.zlagodabackend.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ua.edu.ukma.zlagodabackend.dto.employee.CashierSalesResponse;
import ua.edu.ukma.zlagodabackend.model.Employee;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EmployeeDao {

    private final JdbcTemplate jdbc;

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
            0
    );

    // Менеджер п. 4: Отримати інформацію про усіх працівників, відсортованих за прізвищем
    public List<Employee> findAllSortedBySurname() {
        String sql = "SELECT * FROM Employee ORDER BY empl_surname ASC";
        return jdbc.query(sql, EMPLOYEE_MAPPER);
    }

    // Менеджер п. 5: Отримати інформацію про усіх касирів, відсортованих за прізвищем
    public List<Employee> findCashiersSortedBySurname() {
        String sql = "SELECT * FROM Employee WHERE empl_role = 'cashier' ORDER BY empl_surname ASC";
        return jdbc.query(sql, EMPLOYEE_MAPPER);
    }

    // Менеджер п. 11: Пошук за прізвищем (для контактів)
    public List<Employee> findBySurname(String surname) {
        String sql = "SELECT * FROM Employee WHERE empl_surname LIKE ? ORDER BY empl_surname ASC";
        return jdbc.query(sql, EMPLOYEE_MAPPER, "%" + surname + "%");
    }

    public Optional<Employee> findById(String id) {
        String sql = "SELECT * FROM Employee WHERE id_employee = ?";
        return jdbc.query(sql, EMPLOYEE_MAPPER, id).stream().findFirst();
    }

    // Менеджер п. 1: Введення відомостей про нового працівника
    public void save(Employee e) {
        String sql = """
            INSERT INTO Employee (id_employee, empl_surname, empl_name, empl_patronymic, 
            empl_role, salary, date_of_birth, date_of_start, phone_number, city, street, zip_code, password) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        jdbc.update(sql, e.getIdEmployee(), e.getEmplSurname(), e.getEmplName(), e.getEmplPatronymic(),
                e.getEmplRole(), e.getSalary(), e.getDateOfBirth(), e.getDateOfStart(), e.getPhoneNumber(),
                e.getCity(), e.getStreet(), e.getZipCode(), e.getPassword());
    }

    // Менеджер п. 2: Редагувати дані про працівників
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

    // Менеджер п. 3: Вилучити відомості про працівника
    public void delete(String id) {
        String sql = "DELETE FROM Employee WHERE id_employee = ?";
        jdbc.update(sql, id);
    }
}