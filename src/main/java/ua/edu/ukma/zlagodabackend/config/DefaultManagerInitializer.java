package ua.edu.ukma.zlagodabackend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ua.edu.ukma.zlagodabackend.dao.EmployeeDao;
import ua.edu.ukma.zlagodabackend.model.Employee;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DefaultManagerInitializer implements CommandLineRunner {

    private final EmployeeDao employeeDao;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String managerId = "1";

        if (employeeDao.findById(managerId).isPresent()) {
            return;
        }

        Employee manager = new Employee(
                managerId,
                "Default",
                "Manager",
                "System",
                "manager",
                new BigDecimal("2000.0000"),
                LocalDate.of(1990, 1, 1),
                LocalDate.now(),
                "+380000000001",
                "Kyiv",
                "Main Street",
                "01001",
                passwordEncoder.encode("123456"),
                null
        );

        employeeDao.save(manager);
    }
}
