package ua.edu.ukma.zlagodabackend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ua.edu.ukma.zlagodabackend.dao.EmployeeDao;
import ua.edu.ukma.zlagodabackend.dto.employee.EmployeeRequest;
import ua.edu.ukma.zlagodabackend.exception.ResourceNotFoundException;
import ua.edu.ukma.zlagodabackend.model.Employee;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeDao employeeDao;
    private final PasswordEncoder passwordEncoder;

    public List<Employee> findAll() {
        return employeeDao.findAll();
    }

    public Employee findById(String id) {
        return employeeDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Працівника з ID " + id + " не знайдено"));
    }

    public List<Employee> findCashiers() {
        return employeeDao.findCashiers();
    }

    public List<Employee> findBySurname(String surname) {
        return employeeDao.findBySurname(surname);
    }

    public Employee create(EmployeeRequest request) {
        if (Period.between(request.dateOfBirth(), LocalDate.now()).getYears() < 18) {
            throw new IllegalArgumentException("Працівник повинен бути повнолітнім (18+ років)");
        }

        Employee employee = mapToEntity(request);
        
        if (request.password() != null) {
            employee.setPassword(passwordEncoder.encode(request.password()));
        }

        employeeDao.save(employee);
        return employee;
    }

    public Employee update(String id, EmployeeRequest request) {
        Employee existing = findById(id);
        
        Employee updated = mapToEntity(request);
        updated.setIdEmployee(id);
        updated.setPassword(existing.getPassword());

        employeeDao.update(updated);
        return updated;
    }

    public void delete(String id) {
        findById(id);
        employeeDao.delete(id);
    }

    private Employee mapToEntity(EmployeeRequest request) {
        return new Employee(
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
            null
        );
    }
}