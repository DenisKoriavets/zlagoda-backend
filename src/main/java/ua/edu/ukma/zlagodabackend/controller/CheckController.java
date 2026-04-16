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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ua.edu.ukma.zlagodabackend.dto.check.CheckCreateRequest;
import ua.edu.ukma.zlagodabackend.dto.check.CheckDetailsDto;
import ua.edu.ukma.zlagodabackend.service.CheckService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/checks")
@RequiredArgsConstructor
public class CheckController {

    private final CheckService checkService;

    @GetMapping("/all/sorted-by-print-date-desc")
    @PreAuthorize("hasRole('MANAGER')")
    public List<CheckDetailsDto> getAllChecksSortedByPrintDateDesc() {
        return checkService.getAllChecksSortedByPrintDateDesc();
    }

    @GetMapping("/by-cashier/{id}/sorted-by-print-date-desc")
    @PreAuthorize("hasRole('MANAGER')")
    public List<CheckDetailsDto> getChecksByCashierSortedByPrintDateDesc(@PathVariable String id) {
        return checkService.getChecksByCashierSortedByPrintDateDesc(id);
    }

    @GetMapping("/my/today")
    @PreAuthorize("hasAnyRole('MANAGER', 'CASHIER')")
    public List<CheckDetailsDto> getMyChecksToday(Authentication authentication) {
        return checkService.getMyChecksToday(authentication.getName());
    }

    @GetMapping("/my/by-period")
    @PreAuthorize("hasAnyRole('MANAGER', 'CASHIER')")
    public List<CheckDetailsDto> getMyChecksByPeriod(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return checkService.getChecksByCashierAndPeriod(authentication.getName(), from, to);
    }

    @GetMapping("/by-cashier/{id}/by-period")
    @PreAuthorize("hasRole('MANAGER')")
    public List<CheckDetailsDto> getChecksByCashierForPeriod(
            @PathVariable String id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return checkService.getChecksByCashierAndPeriod(id, from, to);
    }

    @GetMapping("/all/by-period")
    @PreAuthorize("hasRole('MANAGER')")
    public List<CheckDetailsDto> getAllChecksByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return checkService.getAllChecksByPeriod(from, to);
    }

    @GetMapping("/sales-sum/by-cashier/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public Map<String, Object> getSalesSumByCashier(
            @PathVariable String id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return Map.of(
                "cashierId", id,
                "totalSum", checkService.getSalesSumByCashier(id, from, to)
        );
    }

    @GetMapping("/sales-sum/all")
    @PreAuthorize("hasRole('MANAGER')")
    public Map<String, Object> getSalesSumAll(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return Map.of("totalSum", checkService.getSalesSumAll(from, to));
    }

    @GetMapping("/{number}")
    @PreAuthorize("hasAnyRole('MANAGER', 'CASHIER')")
    public CheckDetailsDto getCheckDetails(@PathVariable String number) {
        return checkService.getCheckDetails(number);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('MANAGER', 'CASHIER')")
    public CheckDetailsDto createCheck(
            @Valid @RequestBody CheckCreateRequest request,
            Authentication authentication) {
        return checkService.createCheck(request, authentication.getName());
    }

    @DeleteMapping("/{number}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('MANAGER')")
    public void deleteCheck(@PathVariable String number) {
        checkService.deleteCheck(number);
    }
}
