package tn.driveaway;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class BillingDataSource {
    private static final Logger LOG = LoggerFactory.getLogger(BillingDataSource.class);

    @Bean
    HikariDataSource dataSource(DataSourceProperties properties) throws SQLException {
        String url = properties.determineUrl();
        if (url.startsWith("jdbc:mysql:")) {
            Properties probe = new Properties();
            if (properties.getUsername() != null) {
                probe.setProperty("user", properties.getUsername());
            }
            if (properties.getPassword() != null) {
                probe.setProperty("password", properties.getPassword());
            }
            probe.setProperty("connectTimeout", "2000");
            probe.setProperty("socketTimeout", "3000");
            try (var connection = DriverManager.getConnection(url, probe)) {
                LOG.info("Using the configured MySQL database for billing.");
            } catch (SQLException exception) {
                String state = exception.getSQLState();
                if (state == null || !state.startsWith("08")) {
                    throw exception;
                }
                LOG.warn("MySQL is unreachable (SQLState {}). Billing is using temporary in-memory H2 until restart.", state);
                HikariDataSource fallback = new HikariDataSource();
                fallback.setJdbcUrl("jdbc:h2:mem:facturation;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
                fallback.setUsername("sa");
                return fallback;
            }
        }
        return properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }
}
