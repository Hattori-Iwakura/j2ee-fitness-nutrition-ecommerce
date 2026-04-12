package com.example.j2ee_fitness_nutrition_ecommerce.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

/**
 * Fixes MySQL {@code stock_logs} when Hibernate created both {@code variant_id} and
 * {@code product_variant_id} as NOT NULL FKs; inserts only fill one column and the other triggers 1364.
 */
@Component
@Order(0)
public class StockLogsSchemaMigration implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StockLogsSchemaMigration.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public StockLogsSchemaMigration(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try (Connection c = dataSource.getConnection()) {
            String url = c.getMetaData().getURL();
            if (url == null || (!url.contains("mysql") && !url.contains("mariadb"))) {
                return;
            }
        } catch (Exception e) {
            log.debug("Skipping stock_logs migration (datasource check): {}", e.getMessage());
            return;
        }

        try {
            if (!tableExists("stock_logs")) {
                return;
            }
            // Avoid DROP COLUMN IF EXISTS — some JDBC/MySQL combos surface it as "bad SQL grammar"
            if (!columnExists("stock_logs", "product_variant_id")) {
                return;
            }
            // DROP COLUMN fails if a FK still references the legacy column; drop FKs first.
            dropForeignKeysOnColumn("stock_logs", "product_variant_id");
            jdbcTemplate.execute("ALTER TABLE stock_logs DROP COLUMN product_variant_id");
            log.info("stock_logs: dropped legacy product_variant_id column (JPA uses variant_id)");
        } catch (Exception e) {
            log.warn("stock_logs migration failed — checkout may error with MySQL 1364 on product_variant_id: {}", e.toString());
        }
    }

    /**
     * Removes FK constraints that use this column so {@code DROP COLUMN} can succeed.
     */
    private void dropForeignKeysOnColumn(String table, String column) {
        List<String> constraints = jdbcTemplate.query(
                """
                SELECT DISTINCT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?
                AND REFERENCED_TABLE_NAME IS NOT NULL
                """,
                (rs, rowNum) -> rs.getString(1),
                table,
                column);
        for (String fk : constraints) {
            if (fk == null || fk.isBlank()) {
                continue;
            }
            String safeTable = table.replace("`", "``");
            String safeFk = fk.replace("`", "``");
            jdbcTemplate.execute("ALTER TABLE `" + safeTable + "` DROP FOREIGN KEY `" + safeFk + "`");
            log.info("stock_logs: dropped foreign key `{}` on {}.{}", fk, table, column);
        }
    }

    private boolean tableExists(String table) {
        Integer n = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?
                """,
                Integer.class, table);
        return n != null && n > 0;
    }

    private boolean columnExists(String table, String column) {
        Integer n = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?
                """,
                Integer.class, table, column);
        return n != null && n > 0;
    }
}
