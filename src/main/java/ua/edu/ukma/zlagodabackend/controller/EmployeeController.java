package ua.edu.ukma.zlagodabackend.controller;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ua.edu.ukma.zlagodabackend.dto.employee.CashierSalesResponse;
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

    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public List<Employee> getAllEmployees(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String surname) {
        if (role != null && !role.isBlank()) {
            return employeeService.findByRole(role);
        }
        if (surname != null && !surname.isBlank()) {
            return employeeService.findBySurname(surname.trim());
        }
        return employeeService.findAll();
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('MANAGER', 'CASHIER')")
    public Employee getMyProfile(Authentication authentication) {
        return employeeService.findById(authentication.getName());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'CASHIER')")
    public Employee getEmployeeById(@PathVariable String id) {
        return employeeService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('MANAGER')")
    public Employee createEmployee(@Valid @RequestBody EmployeeCreateRequest request) {
        return employeeService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public Employee updateEmployee(@PathVariable String id, @Valid @RequestBody EmployeeUpdateRequest request) {
        return employeeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('MANAGER')")
    public void deleteEmployee(@PathVariable String id) {
        employeeService.delete(id);
    }

    @GetMapping("/{id}/sales")
    @PreAuthorize("hasRole('MANAGER')") // Звіти — тільки для менеджерів
    public CashierSalesResponse getCashierSalesReport(
        @PathVariable String id,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return employeeService.getCashierSales(id, from, to);
    }
}