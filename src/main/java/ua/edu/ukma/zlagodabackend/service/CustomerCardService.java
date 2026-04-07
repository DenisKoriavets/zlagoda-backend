package ua.edu.ukma.zlagodabackend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.edu.ukma.zlagodabackend.dao.CustomerCardDao;
import ua.edu.ukma.zlagodabackend.dto.customer.CustomerCardRequest;
import ua.edu.ukma.zlagodabackend.exception.ResourceNotFoundException;
import ua.edu.ukma.zlagodabackend.model.CustomerCard;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerCardService {

    private final CustomerCardDao customerCardDao;

    public List<CustomerCard> findAll() {
        return customerCardDao.findAll();
    }

    public CustomerCard findById(String cardNumber) {
        return customerCardDao.findById(cardNumber)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Карту клієнта " + cardNumber + " не знайдено"));
    }

    public List<CustomerCard> findBySurname(String surname) {
        return customerCardDao.findBySurname(surname);
    }

    public List<CustomerCard> findByPercent(int percent) {
        return customerCardDao.findByPercent(percent);
    }

    public CustomerCard create(CustomerCardRequest request) {
        CustomerCard card = mapToEntity(request);
        customerCardDao.save(card);
        return card;
    }

    public CustomerCard update(String cardNumber, CustomerCardRequest request) {
        findById(cardNumber);
        CustomerCard card = mapToEntity(request);
        card.setCardNumber(cardNumber);
        customerCardDao.update(card);
        return card;
    }

    public void delete(String cardNumber) {
        findById(cardNumber);
        customerCardDao.delete(cardNumber);
    }

    public List<CustomerCard> getCustomersByProduct(String upc) {
        return customerCardDao.findByProductUpc(upc);
    }

    private CustomerCard mapToEntity(CustomerCardRequest request) {
        return new CustomerCard(
            request.cardNumber(),
            request.custSurname(),
            request.custName(),
            request.custPatronymic(),
            request.phoneNumber(),
            request.city(),
            request.street(),
            request.zipCode(),
            request.percent()
        );
    }
}