package ua.edu.ukma.zlagodabackend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ua.edu.ukma.zlagodabackend.dao.CheckDao;
import ua.edu.ukma.zlagodabackend.dao.EmployeeDao;
import ua.edu.ukma.zlagodabackend.dto.employee.EmployeeContactResponse;
import ua.edu.ukma.zlagodabackend.dto.employee.EmployeeCreateRequest;
import ua.edu.ukma.zlagodabackend.dto.employee.EmployeeUpdateRequest;
import ua.edu.ukma.zlagodabackend.exception.BusinessValidationException;
import ua.edu.ukma.zlagodabackend.exception.ResourceNotFoundException;
import ua.edu.ukma.zlagodabackend.model.Employee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeDao employeeDao;
    private final CheckDao checkDao;
    private final PasswordEncoder passwordEncoder;

    // Касир п. 15 / Базовий пошук
    public Employee findById(String id) {
        return employeeDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Працівника з ID " + id + " не знайдено"));
    }

    // Менеджер п. 4: Отримати інформацію про усіх працівників, відсортованих за прізвищем
    public List<Employee> findAllSortedBySurname() {
        return employeeDao.findAllSortedBySurname();
    }

    // Менеджер п. 5: Отримати інформацію про усіх касирів, відсортованих за прізвищем
    public List<Employee> findCashiersSortedBySurname() {
        return employeeDao.findCashiersSortedBySurname();
    }

    // Менеджер п. 11: За прізвищем працівника знайти його телефон та адресу
    public List<EmployeeContactResponse> findContactsBySurname(String surname) {
        return employeeDao.findBySurname(surname).stream()
                .map(e -> {
                    // Формуємо ПІБ (по батькові може бути null) [cite: 150]
                    String fullName = Stream.of(e.getEmplSurname(), e.getEmplName(), e.getEmplPatronymic())
                            .filter(Objects::nonNull)
                            .filter(s -> !s.isBlank())
                            .collect(Collectors.joining(" "));

                    // Формуємо складену адресу: місто, вулиця, індекс [cite: 150]
                    String fullAddress = String.join(", ", e.getCity(), e.getStreet(), e.getZipCode());

                    return new EmployeeContactResponse(
                            e.getIdEmployee(),
                            fullName,
                            e.getPhoneNumber(),
                            fullAddress
                    );
                })
                .toList();
    }

    // Менеджер п. 1: Введення відомостей про працівника
    public Employee create(EmployeeCreateRequest request) {
        validateAge(request.dateOfBirth());

        Employee employee = new Employee(
                request.idEmployee(),
                request.emplSurname(),
                request.emplName(),
                request.emplPatronymic(),
                request.emplRole(),
                request.salary(),
                request.dateOfBirth(),
                request.dateOfStart(),
                request.phoneNumber(),
                request.city(),
                request.street(),
                request.zipCode(),
                passwordEncoder.encode(request.password()),
                null
        );

        employeeDao.save(employee);
        return employee;
    }

    // Менеджер п. 2: Оновлення відомостей про працівника
    public Employee update(String id, EmployeeUpdateRequest request) {
        Employee existing = findById(id);

        // ДОДАНО: Перевірка віку при редагуванні, щоб уникнути порушення цілісності
        validateAge(request.dateOfBirth());

        Employee updated = new Employee(
                id,
                request.emplSurname(),
                request.emplName(),
                request.emplPatronymic(),
                request.emplRole(),
                request.salary(),
                request.dateOfBirth(),
                request.dateOfStart(),
                request.phoneNumber(),
                request.city(),
                request.street(),
                request.zipCode(),
                existing.getPassword(), // Пароль залишаємо старим
                null
        );

        employeeDao.update(updated);
        return updated;
    }

    // Менеджер п. 3: Вилучення відомостей про працівника
    public void delete(String id) {
        findById(id);

        // Обмеження цілісності: On Delete No Action для працівників із чеками [cite: 229]
        int checksCount = checkDao.countByEmployeeId(id);
        if (checksCount > 0) {
            throw new BusinessValidationException(
                    "Неможливо видалити працівника: існують чеки, створені цим касиром (" + checksCount + " шт).");
        }
        employeeDao.delete(id);
    }

    private void validateAge(LocalDate dateOfBirth) {
        if (Period.between(dateOfBirth, LocalDate.now()).getYears() < 18) {
            throw new BusinessValidationException("Згідно з правилами цілісності, вік працівника не може бути меншим за 18 років.");
        }
    }
}