package de.eztools.ezdb.shell;

import de.eztools.ezdb.api.model.*;
import de.eztools.ezdb.api.shell.ConnectionService;
import de.eztools.ezdb.api.shell.TaskExecutor;
import de.eztools.ezdb.shell.table.EzTable;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.joining;

@Component
public class TaskExecutorService extends EzdbComponent implements TaskExecutor {

    @Autowired
    @Qualifier("target")
    private ConnectionService targetConnectionService;

    @Autowired
    @Qualifier("source")
    private ConnectionService sourceConnectionService;

    private Map<Parameter, Object> parameterValues = new HashMap<>();
    private int batchSize = DEFAULT_BATCH_SIZE;
    private int fetchSize = DEFAULT_FETCH_SIZE;

    public TaskExecutorService() {
    }

    public TaskExecutorService(ConnectionService targetConnectionService, ConnectionService sourceConnectionService) {
        this.targetConnectionService = targetConnectionService;
        this.sourceConnectionService = sourceConnectionService;
    }

    @Override
    public void visit(CopyTask task) {
        Unchecked.consumer(this::executeCopy).accept(task);
    }


    private void executeCopy(CopyTask task) throws SQLException {
        try (Connection sourceConnection = sourceConnectionService.getConnection();
             Connection targetConnection = targetConnectionService.getConnection();
             PreparedStatement selectStatement = createStatement(task.getSelectStatement(), task.getParameters(),
                     task.getParameterRegex(), sourceConnection);
             PreparedStatement insertStatement = targetConnection.prepareStatement(task.getInsertStatement())) {

            targetConnection.setAutoCommit(false);

            selectStatement.setFetchSize(fetchSize);
            ResultSet selectResultSet = selectStatement.executeQuery();

            int selectedColumnsCount = selectResultSet.getMetaData().getColumnCount();

            BatchProcessor batchProcessor = new BatchProcessor(insertStatement, batchSize);
            while (selectResultSet.next()) {
                for (int col = 1; col <= selectedColumnsCount; col++) {
                    StatementParameters.apply(insertStatement, col, selectResultSet.getObject(col));
                }
                batchProcessor.addBatch();
            }
            batchProcessor.commit();
            targetConnection.setAutoCommit(true);
            printInfo(batchProcessor.getRows() + " row(s) inserted");
        }
    }


    @Override
    public void visit(UpdateTask task) {
        Unchecked.consumer(this::executeUpdate).accept(task);
    }


    private void executeUpdate(UpdateTask task) throws SQLException {
        String statement = task.getStatement();
        Set<Parameter> parameters = task.getParameters();
        String regex = task.getParameterRegex();

        try (Connection connection = targetConnectionService.getConnection();
             PreparedStatement updateStatement = createStatement(statement, parameters, regex, connection)) {
            int result = updateStatement.executeUpdate();
            printInfo(result + " row(s) updated");
        }
    }


    @Override
    public void visit(CsvExportTask task) {
        Unchecked.consumer(this::executeExport).accept(task);
    }


    private void executeExport(CsvExportTask task) throws SQLException, IOException {
        String statement = task.getStatement();
        Set<Parameter> parameters = task.getParameters();
        String regex = task.getParameterRegex();

        try (Connection connection = targetConnectionService.getConnection();
             PreparedStatement selectStatement = createStatement(statement, parameters, regex, connection)) {

            selectStatement.setFetchSize(fetchSize);
            ResultSet selectResultSet = selectStatement.executeQuery();

            File file = new File(task.getFileName());
            CSVPrinter printer = CSVFormat.valueOf(task.getFormat()).withHeader(selectResultSet).print(file, StandardCharsets.UTF_8);
            printer.printRecords(selectResultSet);

            printer.flush();
            printer.close();
            printInfo(file.length() + " bytes written to " + file);
        }
    }


    @Override
    public void visit(PrintTask task) {
        Unchecked.consumer(this::executePrint).accept(task);
    }


    private void executePrint(PrintTask task) throws SQLException {
        String statement = task.getStatement();
        Set<Parameter> parameters = task.getParameters();
        String regex = task.getParameterRegex();

        try (Connection connection = targetConnectionService.getConnection();
             PreparedStatement selectStatement = createStatement(statement, parameters, regex, connection)) {

            selectStatement.setFetchSize(fetchSize);
            ResultSet selectResultSet = selectStatement.executeQuery();

            String table = EzTable.builder()
                    .fromResultSet(selectResultSet)
                    .renderWithRowCount();

            selectResultSet.close();
            print(table);
        }
    }


    @Override
    public void visit(ImportTask task) {
        Unchecked.consumer(this::executeImport).accept(task);
    }

    @Override
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    @Override
    public int getBatchSize() {
        return batchSize;
    }

    @Override
    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }

    @Override
    public int getFetchSize() {
        return fetchSize;
    }

    public Map<Parameter, Object> getParameters() {
        return parameterValues;
    }

    public void setParameters(Map<Parameter, Object> parameterValues) {
        this.parameterValues = parameterValues;
    }

    private void executeImport(ImportTask task) throws Exception {
        Reader in = new FileReader(task.getFileName());
        CSVParser parser = CSVFormat.valueOf(task.getFormat()).withFirstRecordAsHeader().parse(in);

        List<String> columnNames = parser.getHeaderNames();
        String tableName = task.getTableName();
        Set<Parameter> parameters = task.getParameters();
        String regex = task.getParameterRegex();

        String insert = createInsertStatement(tableName, columnNames);

        try (Connection connection = targetConnectionService.getConnection();
             PreparedStatement insertStatement = createStatement(insert, parameters, regex, connection)) {

            BatchProcessor batchProcessor = new BatchProcessor(insertStatement, batchSize);
            for (CSVRecord record : parser) {
                for (int col = 1; col <= record.size(); col++) {
                    StatementParameters.apply(insertStatement, col, record.get(col - 1));
                }
                batchProcessor.addBatch();
            }
            batchProcessor.commit();
            printInfo(batchProcessor.getRows() + " rows inserted into " + tableName);

            connection.setAutoCommit(true);
        } finally {
            parser.close();
        }
    }


    @Override
    public void visit(BinaryExportTask task) {
        Unchecked.consumer(this::executeBinaryExport).accept(task);
    }


    private void executeBinaryExport(BinaryExportTask task) throws Exception {
        String statement = task.getStatement();
        Set<Parameter> parameters = task.getParameters();
        String regex = task.getParameterRegex();

        int rows = 0;
        try (Connection connection = targetConnectionService.getConnection();
             PreparedStatement selectStatement = createStatement(statement, parameters, regex, connection)) {

            selectStatement.setFetchSize(fetchSize);
            ResultSet selectResultSet = selectStatement.executeQuery();

            while (selectResultSet.next()) {
                String dynamicFileName = selectResultSet.getString(task.getFileNameColumnIndex());
                Blob blob = selectResultSet.getBlob(task.getDataColumnIndex());

                InputStream in = blob.getBinaryStream();
                OutputStream out = Files.newOutputStream(Paths.get(task.makeFileName(dynamicFileName)));
                byte[] buff = new byte[4096];
                int len;

                while ((len = in.read(buff)) != -1) {
                    out.write(buff, 0, len);
                }

                out.close();
                in.close();

                rows++;
            }
            printInfo(rows + " file(s) exported");
        }
    }


    private String createInsertStatement(String tableName, List<String> columnNames) {
        return "INSERT INTO " +
                tableName +
                columnNames.stream().collect(joining(", ", " (", ") ")) +
                "VALUES" +
                columnNames.stream().map(col -> "?").collect(joining(", ", " (", ")"));
    }


    private PreparedStatement createStatement(String sql, Set<Parameter> parameters, String parameterRegex,
                                              Connection connection) throws SQLException {

        String processedSql = replaceNoBindParameters(sql, parameters);

        List<String> keys = extractParameterKeys(processedSql, parameterRegex);

        String statementSql = processedSql.replaceAll(parameterRegex, "?");
        PreparedStatement statement = connection.prepareStatement(statementSql);

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            Parameter parameter = getParameter(key);
            Object parameterValue = parameterValues.get(parameter);

            StatementParameters.apply(statement, i + 1, parameterValue);
        }

        return statement;
    }


    private String replaceNoBindParameters(String sql, Set<Parameter> parameters) {

        String statementSql = sql;

        for (Parameter parameter : parameters) {
            if (parameter.getType() == NoBind.class) {
                Object parameterValue = parameterValues.get(parameter);
                statementSql = statementSql.replaceAll(Pattern.quote(parameter.getKey()), parameterValue.toString());
            }
        }

        return statementSql;
    }


    private List<String> extractParameterKeys(String statement, String parameterRegex) {
        List<String> matches = new ArrayList<>();

        Matcher m = Pattern.compile(parameterRegex).matcher(statement);

        while (m.find()) {
            String match = m.group();
            matches.add(match);
        }
        return matches;
    }


    private Parameter getParameter(String key) {
        return parameterValues.keySet().stream()
                .filter(p -> p.getKey().equals(key))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(key + " not found"));
    }
}
