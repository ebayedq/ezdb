package de.eztools.ezdb.shell;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import de.eztools.ezdb.api.shell.ConnectionService;
import de.eztools.ezdb.shell.table.EzTable;

import java.sql.Connection;
import java.util.Properties;

public class PooledConnectionService implements ConnectionService {

    private HikariPooledConnectionSupplier connectionSupplier;

    @Override
    public void connect(Properties properties) {
        try {
            connectionSupplier = new HikariPooledConnectionSupplier(properties);

            //test connection
            Connection connection = connectionSupplier.get();
            connection.close();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void disconnect() {
        if (isConnected()) {
            HikariDataSource dataSource = connectionSupplier.getDataSource();
            if (dataSource != null) {
                dataSource.close();
            }
            connectionSupplier = null;
        }
    }

    @Override
    public boolean isConnected() {
        return connectionSupplier != null;
    }

    @Override
    public Connection getConnection() {
        return connectionSupplier.get();
    }

    @Override
    public String getInfo() {
        if (isConnected()) {
            HikariDataSource dataSource = connectionSupplier.getDataSource();
            if (dataSource != null) {
                HikariPoolMXBean mxBean = dataSource.getHikariPoolMXBean();
                if (mxBean != null) {
                    return EzTable.builder()
                            .fromBean(HikariPoolMXBean.class, mxBean)
                            .render();
                }
            }
        }
        return "N/A";
    }
}
