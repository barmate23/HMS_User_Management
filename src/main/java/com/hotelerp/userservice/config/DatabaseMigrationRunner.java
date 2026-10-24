package com.hotelerp.userservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class DatabaseMigrationRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        log.info("Running automatic database migration check for table 'users'...");
        ensureColumn("users", "must_change_password", "BOOLEAN DEFAULT FALSE");
        ensureColumn("users", "default_password_generated_at", "DATETIME NULL");
    }

    private void ensureColumn(String tableName, String columnName, String columnDefinition) {
        try {
            // First try with IF NOT EXISTS (MySQL 8.0.29+)
            jdbcTemplate.execute("ALTER TABLE " + tableName + " ADD COLUMN IF NOT EXISTS " + columnName + " " + columnDefinition);
            log.info("Verified column '{}.{}' exists.", tableName, columnName);
        } catch (Exception e1) {
            // Fallback for older MySQL versions: attempt standard ADD COLUMN; ignore error if column already exists
            try {
                jdbcTemplate.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDefinition);
                log.info("Added column '{}.{}' successfully.", tableName, columnName);
            } catch (Exception e2) {
                log.debug("Column '{}.{}' check completed: {}", tableName, columnName, e2.getMessage());
            }
        }
    }
}
