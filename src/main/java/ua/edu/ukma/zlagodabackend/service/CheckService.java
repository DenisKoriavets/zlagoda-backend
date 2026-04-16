package ua.edu.ukma.zlagodabackend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.ukma.zlagodabackend.dao.CheckDao;
import ua.edu.ukma.zlagodabackend.dao.SaleDao;
import ua.edu.ukma.zlagodabackend.dao.StoreProductDao;
import ua.edu.ukma.zlagodabackend.dto.check.CheckCreateRequest;
import ua.edu.ukma.zlagodabackend.dto.check.CheckDetailsDto;
import ua.edu.ukma.zlagodabackend.dto.saleItem.SaleItemRequest;
import ua.edu.ukma.zlagodabackend.exception.ResourceNotFoundException;
import ua.edu.ukma.zlagodabackend.model.Check;
import ua.edu.ukma.zlagodabackend.model.CustomerCard;
import ua.edu.ukma.zlagodabackend.model.Sale;
import ua.edu.ukma.zlagodabackend.model.StoreProduct;
import ua.edu.ukma.zlagodabackend.util.CheckNumberGenerator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckService {

    private final CheckDao checkDao;
    private final SaleDao saleDao;
    private final StoreProductDao storeProductDao; 
    
    private final CustomerCardService customerCardService;

    @Transactional
    public CheckDetailsDto createCheck(CheckCreateRequest request, String cashierId) {
        String checkNumber = CheckNumberGenerator.generate();
        BigDecimal sumTotal = BigDecimal.ZERO;
        List<Sale> salesToSave = new ArrayList<>();

        Map<String, Integer> aggregatedItems = request.items().stream()
                .collect(Collectors.toMap(
                        SaleItemRequest::upc,
                        SaleItemRequest::quantity,
                        Integer::sum
                ));

        for (Map.Entry<String, Integer> entry : aggregatedItems.entrySet()) {
            String upc = entry.getKey();
            Integer quantity = entry.getValue();

            StoreProduct sp = storeProductDao.findEntityByUpc(upc)
                    .orElseThrow(() -> new ResourceNotFoundException("Товар з UPC " + upc + " не знайдено"));

            storeProductDao.decreaseQuantity(upc, quantity);

            BigDecimal itemCost = sp.getSellingPrice().multiply(new BigDecimal(quantity));
            sumTotal = sumTotal.add(itemCost);

            salesToSave.add(new Sale(upc, checkNumber, quantity, sp.getSellingPrice()));
        }

        if (request.cardNumber() != null && !request.cardNumber().trim().isEmpty()) {
            CustomerCard card = customerCardService.findById(request.cardNumber());
            
            BigDecimal discountMultiplier = new BigDecimal(100 - card.getPercent())
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            
            sumTotal = sumTotal.multiply(discountMultiplier);
        }

        BigDecimal vat = sumTotal.multiply(new BigDecimal("0.2")).setScale(4, RoundingMode.HALF_UP);
        sumTotal = sumTotal.setScale(4, RoundingMode.HALF_UP);

        Check check = new Check(
                checkNumber, 
                cashierId, 
                request.cardNumber(), 
                LocalDateTime.now(), 
                sumTotal, 
                vat
        );
        checkDao.save(check);

        for (Sale sale : salesToSave) {
            saleDao.save(sale);
        }

        return getCheckDetails(checkNumber);
    }

    public List<CheckDetailsDto> getChecksByCashierAndPeriod(String cashierId, LocalDateTime from, LocalDateTime to) {
        return checkDao.findByCashierAndPeriod(cashierId, from, to);
    }

    public List<CheckDetailsDto> getAllChecksByPeriod(LocalDateTime from, LocalDateTime to) {
        return checkDao.findAllByPeriod(from, to);
    }

    public List<CheckDetailsDto> getMyChecksToday(String cashierId) {
        LocalDate today = LocalDate.now();
        return checkDao.findByCashierAndPeriod(
                cashierId,
                today.atStartOfDay(),
                today.plusDays(1).atStartOfDay().minusNanos(1)
        );
    }

    public BigDecimal getSalesSumByCashier(String cashierId, LocalDateTime from, LocalDateTime to) {
        return checkDao.getSalesSumByCashier(cashierId, from, to);
    }

    public BigDecimal getSalesSumAll(LocalDateTime from, LocalDateTime to) {
        return checkDao.getSalesSumAll(from, to);
    }

    public CheckDetailsDto getCheckDetails(String checkNumber) {
        CheckDetailsDto checkDetails = checkDao.findDetailsById(checkNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Чек " + checkNumber + " не знайдено"));
        
        checkDetails.setItems(saleDao.findByCheckNumber(checkNumber));
        return checkDetails;
    }

    public void deleteCheck(String checkNumber) {
        getCheckDetails(checkNumber);
        checkDao.deleteById(checkNumber);
    }
}