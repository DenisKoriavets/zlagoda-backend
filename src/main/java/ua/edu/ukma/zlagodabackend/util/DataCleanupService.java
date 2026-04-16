package ua.edu.ukma.zlagodabackend.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataCleanupService {

    private final JdbcTemplate jdbcTemplate;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupOldChecks() {
        log.info("Запуск планового очищення застарілих чеків...");

        String sql = "DELETE FROM \"check\" WHERE print_date < CURRENT_DATE - INTERVAL '3 years'";

        int deletedRows = jdbcTemplate.update(sql);

        log.info("Очищення завершено. Видалено чеків: {}", deletedRows);
    }
}