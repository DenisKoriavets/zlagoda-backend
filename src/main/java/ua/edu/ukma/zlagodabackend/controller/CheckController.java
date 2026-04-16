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

    // Касир (Вимога 9): Переглянути список усіх чеків, що створив касир за цей день
    @GetMapping("/my/today")
    @PreAuthorize("hasAnyRole('MANAGER', 'CASHIER')")
    public List<CheckDetailsDto> getMyChecksToday(Authentication authentication) {
        return checkService.getMyChecksToday(authentication.getName());
    }

    // Касир (Вимога 10): Переглянути список усіх чеків, що створив касир за певний період часу
    @GetMapping("/my/by-period")
    @PreAuthorize("hasAnyRole('MANAGER', 'CASHIER')")
    public List<CheckDetailsDto> getMyChecksByPeriod(
        Authentication authentication,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return checkService.getChecksByCashierAndPeriod(authentication.getName(), from, to);
    }

    // Менеджер (Вимога 17): Отримати інформацію про усі чеки, створені певним касиром за певний період часу
    @GetMapping("/by-cashier/{id}/by-period")
    @PreAuthorize("hasRole('MANAGER')")
    public List<CheckDetailsDto> getChecksByCashierForPeriod(
        @PathVariable String id,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return checkService.getChecksByCashierAndPeriod(id, from, to);
    }

    // Менеджер (Вимога 18): Отримати інформацію про усі чеки, створені усіма касирами за певний період часу
    @GetMapping("/all/by-period")
    @PreAuthorize("hasRole('MANAGER')")
    public List<CheckDetailsDto> getAllChecksByPeriod(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return checkService.getAllChecksByPeriod(from, to);
    }

    // Менеджер (Вимога 19): Визначити загальну суму проданих товарів з чеків, створених певним касиром за певний період часу
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

    // Менеджер (Вимога 20): Визначити загальну суму проданих товарів з чеків, створених усіма касиром за певний період часу
    @GetMapping("/sales-sum/all")
    @PreAuthorize("hasRole('MANAGER')")
    public Map<String, Object> getSalesSumAll(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return Map.of("totalSum", checkService.getSalesSumAll(from, to));
    }

    // Касир (Вимога 11): За номером чеку вивести усю інформацію про даний чек (назва, к-сть та ціна товарів)
    @GetMapping("/{number}")
    @PreAuthorize("hasAnyRole('MANAGER', 'CASHIER')")
    public CheckDetailsDto getCheckDetails(@PathVariable String number) {
        return checkService.getCheckDetails(number);
    }

    // Касир (Вимога 7): Здійснювати продаж товарів (додавання чеків)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('MANAGER', 'CASHIER')")
    public CheckDetailsDto createCheck(
        @Valid @RequestBody CheckCreateRequest request,
        Authentication authentication) {
        return checkService.createCheck(request, authentication.getName());
    }

    // Менеджер (Вимога 3): Видаляти дані про чеки
    @DeleteMapping("/{number}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('MANAGER')")
    public void deleteCheck(@PathVariable String number) {
        checkService.deleteCheck(number);
    }
}