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
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'CASHIER')")
    public List<CustomerCard> getAllCards(
            @RequestParam(required = false) String surname,
            @RequestParam(required = false) Integer percent,
            @RequestParam(required = false, defaultValue = "surname") String sort) {
        if (surname != null) return customerCardService.findBySurname(surname);
        if (percent != null) return customerCardService.findByPercent(percent);
        return customerCardService.findAll(sort);
    }

    @GetMapping("/{number}")
    @PreAuthorize("hasAnyRole('MANAGER', 'CASHIER')")
    public CustomerCard getCardByNumber(@PathVariable String number) {
        return customerCardService.findById(number);
    }

    @GetMapping("/sorted-by-surname")
    @PreAuthorize("hasAnyRole('MANAGER', 'CASHIER')")
    public List<CustomerCard> getCardsSortedBySurname() {
        return customerCardService.findAll();
    }

    @GetMapping("/sorted-by-percent")
    @PreAuthorize("hasAnyRole('MANAGER', 'CASHIER')")
    public List<CustomerCard> getCardsSortedByPercent() {
        return customerCardService.findAll("percent");
    }

    @GetMapping("/search-by-surname/{surname}")
    @PreAuthorize("hasAnyRole('MANAGER', 'CASHIER')")
    public List<CustomerCard> searchCardsBySurname(@PathVariable String surname) {
        return customerCardService.findBySurname(surname.trim());
    }

    @GetMapping("/by-percent/{percent}/sorted-by-surname")
    @PreAuthorize("hasAnyRole('MANAGER', 'CASHIER')")
    public List<CustomerCard> getCardsByPercentSortedBySurname(@PathVariable Integer percent) {
        return customerCardService.findByPercent(percent);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('MANAGER', 'CASHIER')")
    public CustomerCard createCard(@Valid @RequestBody CustomerCardRequest request) {
        return customerCardService.create(request);
    }

    @PutMapping("/{number}")
    @PreAuthorize("hasAnyRole('MANAGER', 'CASHIER')")
    public CustomerCard updateCard(@PathVariable String number, @Valid @RequestBody CustomerCardRequest request) {
        return customerCardService.update(number, request);
    }

    @DeleteMapping("/{number}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('MANAGER')")
    public void deleteCard(@PathVariable String number) {
        customerCardService.delete(number);
    }

    @GetMapping("/by-product/{upc}")
    @PreAuthorize("hasRole('MANAGER')")
    public List<CustomerCard> getCustomersByProduct(@PathVariable String upc) {
        return customerCardService.getCustomersByProduct(upc);
    }
}