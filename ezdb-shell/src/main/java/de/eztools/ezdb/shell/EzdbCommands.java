package de.eztools.ezdb.shell;

import de.eztools.ezdb.api.model.NoBind;
import de.eztools.ezdb.api.model.Parameter;
import de.eztools.ezdb.api.model.Task;
import de.eztools.ezdb.api.shell.ConnectionService;
import de.eztools.ezdb.api.shell.TaskExecutor;
import de.eztools.ezdb.shell.table.EzTable;
import de.eztools.ezdb.shell.xml.XmlBinaryExportTask;
import de.eztools.ezdb.shell.xml.XmlCopyTask;
import de.eztools.ezdb.shell.xml.XmlCsvExportTask;
import de.eztools.ezdb.shell.xml.XmlImportTask;
import de.eztools.ezdb.shell.xml.XmlParameter;
import de.eztools.ezdb.shell.xml.XmlPrintTask;
import de.eztools.ezdb.shell.xml.XmlSuite;
import de.eztools.ezdb.shell.xml.XmlUpdateTask;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.csv.CSVFormat;
import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.shell.Availability;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.FileValueProvider;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.standard.ValueProvider;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.springframework.shell.standard.ShellOption.NULL;

@ShellComponent("ezdb")
public class EzdbCommands extends EzdbComponent {

    private final TaskExecutor taskExecutor;
    private final ConnectionService targetConnectionService;
    private final ConnectionService sourceConnectionService;

    public EzdbCommands(TaskExecutor taskExecutor,
                        @Qualifier("target") ConnectionService targetConnectionService,
                        @Qualifier("source") ConnectionService sourceConnectionService) {
        this.taskExecutor = taskExecutor;
        this.targetConnectionService = targetConnectionService;
        this.sourceConnectionService = sourceConnectionService;
    }

    @ShellMethod(value = "Create target database connection", key = {"connect", "connect-target"})
    public String connectTarget(@ShellOption(help = "JDBC database URL") String url,
                                @ShellOption(help = "Will be prompted of not supplied", defaultValue = NULL) String user) {
        connect(targetConnectionService, url, user);
        return "Connection successful";
    }

    @ShellMethod(value = "Create source database connection (only needed for copy tasks)")
    public String connectSource(@ShellOption(help = "JDBC database URL") String url,
                                @ShellOption(help = "Will be prompted of not supplied", defaultValue = NULL) String user) {
        connect(sourceConnectionService, url, user);
        return "Connection successful";
    }

    private void connect(ConnectionService connectionService, String url, String user) {
        String username = user == null ? prompt("Username:", false) : user;
        String password = prompt("Password:", true);

        Properties properties = new Properties();
        properties.put("url", url);
        properties.put("user", username);
        properties.put("password", password);

        connectionService.connect(properties);
    }

    public Availability connectTargetAvailability() {
        return !targetConnectionService.isConnected()
                ? Availability.available()
                : Availability.unavailable("a target database connection was already established");
    }

    public Availability connectSourceAvailability() {
        return !sourceConnectionService.isConnected()
                ? Availability.available()
                : Availability.unavailable("a source database connection was already established");
    }

    @ShellMethod("Close database connections")
    public void disconnect() {
        targetConnectionService.disconnect();
        sourceConnectionService.disconnect();
    }

    @ShellMethod("List known drivers")
    public String drivers() {
        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{"Driver", "Version", "Compliant"});
        for (Driver driver : ServiceLoader.load(Driver.class)) {
            String name = driver.getClass().getName();
            String version = driver.getMajorVersion() + "." + driver.getMinorVersion();
            boolean compliant = driver.jdbcCompliant();

            rows.add(new Object[]{name, version, compliant});
        }
        Object[][] array = rows.toArray(new Object[0][]);
        return EzTable.builder()
                .fromArray(array)
                .renderWithRowCount();
    }

    @ShellMethod("Sets a parameter that can be used in task files")
    public void setParameter(@ShellOption(help = "Parameter name. The case must match that of the task(s)") String parameterName,
                             @ShellOption(help = "Parameter value") String parameterValue,
                             @ShellOption(help = "Type the value will be converted to", defaultValue = "de.eztools.ezdb.api.model.NoBind") String type) {
        try {
            Class<?> parameterType = Class.forName(type);
            Object typedValue = convertToType(parameterValue, parameterType);
            XmlParameter parameter = new XmlParameter();
            parameter.setName(parameterName);
            parameter.setType(parameterType);
            taskExecutor.getParameters().put(parameter, typedValue);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @ShellMethod("Remove parameter")
    public void unsetParameter(@ShellOption(help = "Parameter name", valueProvider = ParameterNameValueProvider.class) String parameterName) {
        Map<Parameter, Object> parameterValues = taskExecutor.getParameters();
        parameterValues.keySet().stream()
                .filter(k -> k.getName().equals(parameterName))
                .findFirst()
                .ifPresent(parameterValues::remove);
    }

    @ShellMethod("List parameters")
    public String parameters() {
        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{"Name", "Value", "Type"});

        taskExecutor.getParameters().entrySet().stream()
                .map(e -> new Object[]{e.getKey().getName(), e.getValue(), e.getKey().getType().getSimpleName()})
                .forEach(rows::add);

        Object[][] array = rows.toArray(new Object[0][]);
        return EzTable.builder()
                .fromArray(array)
                .renderWithRowCount();
    }

    @ShellMethod("Sets JDBC batch size")
    public void setBatchSize(@ShellOption(help = "A value less that 1 resets to the default batch size (1000)") int batchSize) {
        this.taskExecutor.setBatchSize(batchSize < 1 ? TaskExecutor.DEFAULT_BATCH_SIZE : batchSize);
    }

    @ShellMethod("Sets JDBC fetch size")
    public void setFetchSize(@ShellOption(help = "A value less that 1 resets to the default fetch size (1000)") int fetchSize) {
        this.taskExecutor.setFetchSize(fetchSize < 1 ? TaskExecutor.DEFAULT_FETCH_SIZE : fetchSize);
    }

    @ShellMethod("Execute select")
    public void print(@ShellOption(help = "SELECT statement") String sql) {
        XmlPrintTask task = new XmlPrintTask();
        task.setStatement(sql);
        task.setParameters(taskExecutor.getParameters().keySet());
        execute(task);
    }

    @ShellMethod("Execute update")
    public void update(@ShellOption(help = "UPDATE statement") String sql) {
        XmlUpdateTask task = new XmlUpdateTask();
        task.setStatement(sql);
        task.setParameters(taskExecutor.getParameters().keySet());
        execute(task);
    }

    @ShellMethod("Export data to a CSV file")
    public void exportCsv(@ShellOption(help = "SELECT statement") String sql,
                          @ShellOption(help = "Path of the generated file") File fileName,
                          @ShellOption(help = "CSV format. See org.apache.commons.csv.CSVFormat.Predefined", defaultValue = XmlCsvExportTask.DEFAULT_FORMAT, valueProvider = CsvFormatValueProvider.class) CSVFormat.Predefined format) {

        XmlCsvExportTask task = new XmlCsvExportTask();
        task.setStatement(sql);
        task.setFormat(format.name());
        task.setFileName(fileName.getPath());
        task.setParameters(taskExecutor.getParameters().keySet());
        execute(task);
    }

    @ShellMethod("Export binary data to file(s)")
    public void exportBinary(@ShellOption(help = "SELECT statement") String sql,
                             @ShellOption(help = "ResultSet column which the file name will be generated from") int fileNameColumnIndex,
                             @ShellOption(help = "ResultSet column which contains the blob data") int dataColumnIndex,
                             @ShellOption(help = "Constant to be used as prefix for file name (e.g. a folder like 'myfolder/')", defaultValue = "") String filePrefix,
                             @ShellOption(help = "Constant to be used as suffix for file name (e.g. a filetype like '.xml')", defaultValue = "") String fileSuffix) {

        XmlBinaryExportTask task = new XmlBinaryExportTask();
        task.setStatement(sql);
        task.setFileNameColumnIndex(fileNameColumnIndex);
        task.setDataColumnIndex(dataColumnIndex);
        task.setFilePrefix(filePrefix);
        task.setFileSuffix(fileSuffix);
        task.setParameters(taskExecutor.getParameters().keySet());
        execute(task);
    }

    @ShellMethod("Import from CSV file")
    public void importCsv(@ShellOption(help = "Target table name") String tableName,
                          @ShellOption(help = "File to be inserted", valueProvider = FileValueProvider.class) File fileName,
                          @ShellOption(help = "CSV format. See org.apache.commons.csv.CSVFormat.Predefined", defaultValue = XmlImportTask.DEFAULT_FORMAT, valueProvider = CsvFormatValueProvider.class) CSVFormat.Predefined format) {

        XmlImportTask task = new XmlImportTask();
        task.setTableName(tableName);
        task.setFileName(fileName.getPath());
        task.setFormat(format.name());
        task.setParameters(taskExecutor.getParameters().keySet());
        execute(task);
    }

    @ShellMethod("Copy data between databases")
    public void copy(@ShellOption(help = "SELECT statement for source") String select,
                     @ShellOption(help = "INSERT statement for target") String insert) {

        XmlCopyTask task = new XmlCopyTask();
        task.setSelectStatement(select);
        task.setInsertStatement(insert);
        task.setParameters(taskExecutor.getParameters().keySet());
        execute(task);
    }

    @ShellMethod("Execute task file")
    public String execute(@ShellOption(help = "XML task file or directory contains task files") File taskFile) throws Exception {
        JAXBContext jc = JAXBContextFactory.createContext(new Class[]{XmlSuite.class}, null);
        Unmarshaller unmarshaller = jc.createUnmarshaller();

        List<Task> tasks = pathToFileList(taskFile).stream()
                .map(Unchecked.function(unmarshaller::unmarshal))
                .map(Task.class::cast)
                .collect(Collectors.toList());

        tasks.stream()
                .map(Task::flatten)
                .flatMap(Collection::stream)
                .forEach(this::execute);

        return "Done";
    }

    public List<File> pathToFileList(File path) throws IOException {
        if (path.isFile()) {
            return Collections.singletonList(path);
        }

        return StreamSupport.stream(Files.newDirectoryStream(path.toPath(), "*.xml").spliterator(), false)
                .map(Path::toFile)
                .sorted(Comparator.comparing(File::getName))
                .collect(Collectors.toList());
    }

    @ShellMethod("Print connection pool status")
    public String status() {
        return "Source:" +
                "\n" +
                sourceConnectionService.getInfo() +
                "\n" +
                "Target:" +
                "\n" +
                targetConnectionService.getInfo();
    }

    private void execute(Task task) {
        task.accept(taskExecutor);
    }

    private Object convertToType(String stringValue, Class<?> type) {
        return type == NoBind.class ? stringValue : ConvertUtils.convert(stringValue, type);
    }

    @ShellMethodAvailability({"print", "update", "export-csv", "export-binary", "import-csv", "execute"})
    public Availability operationsAvailability() {
        return targetConnectionService.isConnected()
                ? Availability.available()
                : Availability.unavailable("no database connection was established");
    }

    @ShellMethodAvailability({"copy"})
    public Availability copyAvailability() {
        return targetConnectionService.isConnected() && sourceConnectionService.isConnected()
                ? Availability.available()
                : Availability.unavailable("no database connection was established");
    }

    @Component
    class ParameterNameValueProvider implements ValueProvider {

        @Override
        public List<CompletionProposal> complete(CompletionContext completionContext) {
            return taskExecutor.getParameters().keySet().stream()
                    .map(Parameter::getName)
                    .distinct()
                    .sorted()
                    .map(CompletionProposal::new)
                    .collect(Collectors.toList());
        }
    }
}
