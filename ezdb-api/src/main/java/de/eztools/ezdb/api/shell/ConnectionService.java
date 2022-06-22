package de.eztools.ezdb.api.shell;

import java.sql.Connection;
import java.util.Properties;

public interface ConnectionService {

    void connect(Properties properties);

    void disconnect();

    boolean isConnected();

    Connection getConnection();

    String getInfo();
}
