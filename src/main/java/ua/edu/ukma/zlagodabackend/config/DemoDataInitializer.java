package ua.edu.ukma.zlagodabackend.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ua.edu.ukma.zlagodabackend.dao.*;
import ua.edu.ukma.zlagodabackend.model.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DemoDataInitializer implements CommandLineRunner {

    private static final String DEMO_PASSWORD = "123456";
    private final EmployeeDao employeeDao;
    private final CategoryDao categoryDao;
    private final ProductDao productDao;
    private final StoreProductDao storeProductDao;
    private final CustomerCardDao customerCardDao;
    private final CheckDao checkDao;
    private final SaleDao saleDao;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Розпочато перевірку та наповнення бази даних тестовими даними...");

        ensureEmployee("1", "Адміністратор", "Системний", null, "manager", "35000", LocalDate.of(1990, 1, 1), "+380500000001");
        ensureEmployee("2", "Коваленко", "Іван", "Петрович", "cashier", "18000", LocalDate.of(1995, 5, 10), "+380500000002");
        ensureEmployee("3", "Мельник", "Олена", "Сергіївна", "cashier", "18500", LocalDate.of(1998, 12, 20), "+380500000003");

        Category catDairy = ensureCategory("Молочні продукти");
        Category catBakery = ensureCategory("Випічка");
        Category catMeat = ensureCategory("М'ясні вироби");
        Category catDrinks = ensureCategory("Напої");
        Category catGrocery = ensureCategory("Бакалія");

        Product milk = ensureProduct(catDairy.getCategoryNumber(), "Молоко 2.5%", "Галичина", "Пакет 900г");
        Product bread = ensureProduct(catBakery.getCategoryNumber(), "Батон Київський", "Київхліб", "Нарізаний, 500г");
        Product sausage = ensureProduct(catMeat.getCategoryNumber(), "Ковбаса Лікарська", "Ятрань", "Вищий сорт");
        Product water = ensureProduct(catDrinks.getCategoryNumber(), "Вода Моршинська", "IDS", "1.5л негазирована");
        Product pasta = ensureProduct(catGrocery.getCategoryNumber(), "Макарони", "Чумак", "Спіральки 400г");

        ensureStoreProduct("100000000001", null, milk.getIdProduct(), "45.50", 50, false);
        ensureStoreProduct("100000000002", null, milk.getIdProduct(), "36.40", 20, true);
        linkStoreProducts("100000000001", "100000000002");

        ensureStoreProduct("200000000001", null, water.getIdProduct(), "21.00", 150, false);
        ensureStoreProduct("300000000002", null, sausage.getIdProduct(), "180.00", 15, true);
        ensureStoreProduct("400000000001", null, bread.getIdProduct(), "28.00", 30, false);
        ensureStoreProduct("500000000001", null, pasta.getIdProduct(), "42.00", 100, false);

        ensureCustomer("CARD000000001", "Шевченко", "Андрій", "Миколайович", "+380670000001", "Київ", "Хрещатик 1", "01001", 10);
        ensureCustomer("CARD000000002", "Франко", "Яна", "Віталіївна", "+380670000002", "Львів", "Франка 10", "79000", 5);

        ensureFullCheck("CHK0000001", "2", "CARD000000001", LocalDateTime.now().minusDays(1),
                List.of(
                        new Sale("100000000001", "CHK0000001", 2, new BigDecimal("45.50")),
                        new Sale("200000000001", "CHK0000001", 1, new BigDecimal("21.00"))
                ));

        ensureFullCheck("CHK0000002", "3", null, LocalDateTime.now().minusHours(5),
                List.of(
                        new Sale("500000000001", "CHK0000002", 3, new BigDecimal("42.00")),
                        new Sale("400000000001", "CHK0000002", 1, new BigDecimal("28.00"))
                ));

        log.info("Наповнення бази завершено успішно.");
    }

    private void ensureEmployee(String id, String surname, String name, String patronymic, String role, String salary, LocalDate birth, String phone) {
        if (employeeDao.findById(id).isPresent()) {
            jdbcTemplate.update("UPDATE Employee SET password = ? WHERE id_employee = ?", passwordEncoder.encode(DEMO_PASSWORD), id);
            return;
        }
        Employee e = new Employee(id, surname, name, patronymic, role, new BigDecimal(salary), birth, LocalDate.now().minusYears(1), phone, "Київ", "Центральна", "01001", passwordEncoder.encode(DEMO_PASSWORD), null);
        employeeDao.save(e);
    }

    private Category ensureCategory(String name) {
        return categoryDao.findAllSortedByName().stream()
                .filter(c -> c.getCategoryName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> {
                    Category c = new Category();
                    c.setCategoryName(name);
                    return categoryDao.save(c);
                });
    }

    private Product ensureProduct(int categoryId, String name, String producer, String chars) {
        try {
            int id = jdbcTemplate.queryForObject("SELECT id_product FROM Product WHERE product_name = ? AND producer = ?", Integer.class, name, producer);
            Product p = new Product();
            p.setIdProduct(id);
            p.setProductName(name);
            p.setProducer(producer);
            p.setCharacteristics(chars);
            p.setCategoryNumber(categoryId);
            return p;
        } catch (EmptyResultDataAccessException e) {
            Product p = new Product();
            p.setProductName(name);
            p.setProducer(producer);
            p.setCharacteristics(chars);
            p.setCategoryNumber(categoryId);
            return productDao.save(p);
        }
    }

    private void ensureStoreProduct(String upc, String upcProm, int productId, String price, int qty, boolean promo) {
        if (storeProductDao.findEntityByUpc(upc).isPresent()) return;
        storeProductDao.save(new StoreProduct(upc, upcProm, productId, new BigDecimal(price), qty, promo));
    }

    private void linkStoreProducts(String regularUpc, String promoUpc) {
        jdbcTemplate.update("UPDATE Store_Product SET UPC_prom = ? WHERE UPC = ?", promoUpc, regularUpc);
    }

    private void ensureCustomer(String card, String surname, String name, String patr, String phone, String city, String street, String zip, int pct) {
        if (customerCardDao.findById(card).isPresent()) return;
        customerCardDao.save(new CustomerCard(card, surname, name, patr, phone, city, street, zip, pct, null));
    }

    private void ensureFullCheck(String checkNum, String emplId, String cardNum, LocalDateTime date, List<Sale> items) {
        Integer exists = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM \"check\" WHERE check_number = ?", Integer.class, checkNum);
        if (exists != null && exists > 0) return;

        BigDecimal total = items.stream()
                .map(s -> s.getSellingPrice().multiply(BigDecimal.valueOf(s.getProductNumber())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal vat = total.multiply(new BigDecimal("0.2"));

        Check check = new Check(checkNum, emplId, cardNum, date, total, vat);
        checkDao.save(check);
        for (Sale s : items) {
            saleDao.save(s);
        }
    }
}