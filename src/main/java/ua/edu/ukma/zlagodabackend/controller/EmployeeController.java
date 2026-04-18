package ua.edu.ukma.zlagodabackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ua.edu.ukma.zlagodabackend.dto.employee.EmployeeContactResponse;
import ua.edu.ukma.zlagodabackend.dto.employee.EmployeeCreateRequest;
import ua.edu.ukma.zlagodabackend.dto.employee.EmployeeUpdateRequest;
import ua.edu.ukma.zlagodabackend.model.Employee;
import ua.edu.ukma.zlagodabackend.service.EmployeeService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    // Введення (Менеджер, п. 1)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('MANAGER')")
    public Employee createEmployee(@Valid @RequestBody EmployeeCreateRequest request) {
        return employeeService.create(request);
    }

    // Оновлення (Менеджер, п. 2)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public Employee updateEmployee(@PathVariable String id, @Valid @RequestBody EmployeeUpdateRequest request) {
        return employeeService.update(id, request);
    }

    // Вилучення (Менеджер, п. 3)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('MANAGER')")
    public void deleteEmployee(@PathVariable String id) {
        employeeService.delete(id);
    }

    // Менеджер, п. 4: Отримати інформацію про усіх працівників, відсортованих за прізвищем
    @GetMapping("/sorted-by-surname")
    @PreAuthorize("hasRole('MANAGER')")
    public List<Employee> getEmployeesSortedBySurname() {
        return employeeService.findAllSortedBySurname();
    }

    // Менеджер, п. 5: Отримати інформацію про усіх працівників, що займають посаду касира, відсортованих за прізвищем
    @GetMapping("/cashiers/sorted-by-surname")
    @PreAuthorize("hasRole('MANAGER')")
    public List<Employee> getCashiersSortedBySurname() {
        return employeeService.findCashiersSortedBySurname();
    }

    // Менеджер, пошук у переліку працівників за прізвищем
    @GetMapping("/search-by-surname/{surname}")
    @PreAuthorize("hasRole('MANAGER')")
    public List<Employee> searchEmployeesBySurname(@PathVariable String surname) {
        return employeeService.findBySurname(surname.trim());
    }

    // Менеджер, п. 11: За прізвищем працівника знайти його телефон та адресу
    @GetMapping("/contacts/{surname}")
    @PreAuthorize("hasRole('MANAGER')")
    public List<EmployeeContactResponse> searchEmployeeContactsBySurname(@PathVariable String surname) {
        return employeeService.findContactsBySurname(surname.trim());
    }

    // Касир, п. 15: Можливість отримати усю інформацію про себе
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('MANAGER', 'CASHIER')")
    public Employee getMyProfile(Authentication authentication) {
        return employeeService.findById(authentication.getName());
    }
}