package ua.edu.ukma.zlagodabackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ua.edu.ukma.zlagodabackend.dto.employee.CashierSalesResponse;
import ua.edu.ukma.zlagodabackend.dto.employee.EmployeeCreateRequest;
import ua.edu.ukma.zlagodabackend.dto.employee.EmployeeUpdateRequest;
import ua.edu.ukma.zlagodabackend.model.Employee;
import ua.edu.ukma.zlagodabackend.service.EmployeeService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    @GetMapping("/sorted-by-surname")
    @PreAuthorize("hasRole('MANAGER')")
    public List<Employee> getEmployeesSortedBySurname() {
        return employeeService.findAll();
    }

    @GetMapping("/cashiers/sorted-by-surname")
    @PreAuthorize("hasRole('MANAGER')")
    public List<Employee> getCashiersSortedBySurname() {
        return employeeService.findCashiers();
    }

    @GetMapping("/search-by-surname/{surname}")
    @PreAuthorize("hasRole('MANAGER')")
    public List<Employee> searchEmployeesBySurname(@PathVariable String surname) {
        return employeeService.findBySurname(surname.trim());
    }

    @GetMapping("/search-by-surname/{surname}/contacts")
    @PreAuthorize("hasRole('MANAGER')")
    public List<Map<String, String>> searchEmployeeContactsBySurname(@PathVariable String surname) {
        return employeeService.findBySurname(surname.trim()).stream()
                .map(e -> Map.of(
                        "idEmployee", e.getIdEmployee(),
                        "fullName", String.join(" ", e.getEmplSurname(), e.getEmplName()),
                        "phoneNumber", e.getPhoneNumber(),
                        "address", String.join(", ", e.getCity(), e.getStreet(), e.getZipCode())
                ))
                .toList();
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