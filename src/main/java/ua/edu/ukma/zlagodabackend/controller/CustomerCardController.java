package ua.edu.ukma.zlagodabackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ua.edu.ukma.zlagodabackend.dto.customer.CustomerCardRequest;
import ua.edu.ukma.zlagodabackend.model.CustomerCard;
import ua.edu.ukma.zlagodabackend.service.CustomerCardService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customer-cards")
@RequiredArgsConstructor
public class CustomerCardController {

    private final CustomerCardService customerCardService;

    // Менеджер (Вимога 7) / Касир (Вимога 3):
    // Отримати інформацію про усіх постійних клієнтів, відсортованих за прізвищем
    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'CASHIER')")
    public List<CustomerCard> getAllCards() {
        return customerCardService.findAll();
    }

    @GetMapping("/{number}")
    @PreAuthorize("hasAnyRole('MANAGER', 'CASHIER')")
    public CustomerCard getCardByNumber(@PathVariable String number) {
        return customerCardService.findById(number);
    }

    // Касир (Вимога 6): Здійснити пошук постійних клієнтів за прізвищем
    @GetMapping("/search-by-surname/{surname}")
    @PreAuthorize("hasAnyRole('MANAGER', 'CASHIER')")
    public List<CustomerCard> searchCardsBySurname(@PathVariable String surname) {
        return customerCardService.findBySurname(surname.trim());
    }

    // Менеджер (Вимога 12): Отримати інформацію про усіх постійних клієнтів,
    // що мають карту клієнта із певним відсотком, посортованих за прізвищем
    @GetMapping("/by-percent/{percent}/sorted-by-surname")
    @PreAuthorize("hasAnyRole('MANAGER', 'CASHIER')")
    public List<CustomerCard> getCardsByPercentSortedBySurname(@PathVariable Integer percent) {
        return customerCardService.findByPercent(percent);
    }

    // Менеджер (Вимога 1) / Касир (Вимога 8): Додавати дані про клієнтів
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('MANAGER', 'CASHIER')")
    public CustomerCard createCard(@Valid @RequestBody CustomerCardRequest request) {
        return customerCardService.create(request);
    }

    // Менеджер (Вимога 2) / Касир (Вимога 8): Редагувати дані про клієнтів
    @PutMapping("/{number}")
    @PreAuthorize("hasAnyRole('MANAGER', 'CASHIER')")
    public CustomerCard updateCard(@PathVariable String number, @Valid @RequestBody CustomerCardRequest request) {
        return customerCardService.update(number, request);
    }

    // Менеджер (Вимога 3): Видаляти дані про клієнтів
    @DeleteMapping("/{number}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('MANAGER')")
    public void deleteCard(@PathVariable String number) {
        customerCardService.delete(number);
    }
}