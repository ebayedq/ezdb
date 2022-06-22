package de.eztools.ezdb.shell.table;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface TableSource {

    TableRenderer fromArray(Object[][] data);

    TableRenderer fromResultSet(ResultSet resultSet) throws SQLException;

    <T> TableRenderer fromBean(Class<T> type, T t);
}