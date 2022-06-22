package de.eztools.ezdb.shell;

import java.sql.PreparedStatement;
import java.sql.SQLException;

final class BatchProcessor {

    private int rows;
    private final int batchSize;
    private final PreparedStatement statement;

    public BatchProcessor(PreparedStatement statement, int batchSize) {
        this.statement = statement;
        this.batchSize = batchSize;
    }

    public void addBatch() throws SQLException {
        statement.addBatch();
        rows++;

        if (rows % batchSize == 0) {
            commit();
        }
    }

    public void commit() throws SQLException {
        statement.executeBatch();
        statement.getConnection().commit();
    }

    public int getRows() {
        return rows;
    }
}
