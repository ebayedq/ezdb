package de.eztools.ezdb.shell;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.function.Supplier;

public class HikariPooledConnectionSupplier implements Supplier<Connection> {

    private HikariDataSource dataSource;
    private final HikariConfig config;

    public HikariPooledConnectionSupplier(Properties properties) {
        this.config = createHikariConfig(properties);
    }

    private HikariConfig createHikariConfig(Properties properties) {
        HikariConfig hc = new HikariConfig();
        hc.setJdbcUrl(properties.getProperty("url"));
        hc.setUsername(properties.getProperty("user"));
        hc.setPassword(properties.getProperty("password"));
        hc.setMaximumPoolSize(1);
        hc.setMinimumIdle(0);

        return hc;
    }

    @Override
    public Connection get() {
        try {
            if (dataSource == null) {
                dataSource = new HikariDataSource(config);
            }
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }
}
