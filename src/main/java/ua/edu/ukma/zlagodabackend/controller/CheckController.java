package ua.edu.ukma.zlagodabackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ua.edu.ukma.zlagodabackend.dto.check.CheckCreateRequest;
import ua.edu.ukma.zlagodabackend.dto.check.CheckDetailsDto;
import ua.edu.ukma.zlagodabackend.service.CheckService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/checks")
@RequiredArgsConstructor
public class CheckController {

    private final CheckService checkService;

    @GetMapping
    public List<CheckDetailsDto> getAllChecks(
            @RequestParam(required = false) String cashier,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        return checkService.getChecks(cashier, from, to);
    }

    @GetMapping("/my")
    public List<CheckDetailsDto> getMyChecks(
            @RequestParam String cashierId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        return checkService.getChecks(cashierId, from, to);
    }

    @GetMapping("/{number}")
    public CheckDetailsDto getCheckDetails(@PathVariable String number) {
        return checkService.getCheckDetails(number);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CheckDetailsDto createCheck(
            @Valid @RequestBody CheckCreateRequest request,
            @RequestParam String cashierId) {

        return checkService.createCheck(request, cashierId);
    }

    @DeleteMapping("/{number}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCheck(@PathVariable String number) {
        checkService.deleteCheck(number);
    }
}