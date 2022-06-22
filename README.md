# ezDB

ezDB is a utility to execute arbitrary SQL statements over JDBC. 
It can be used to create databases, update or export data as well as copy data between two databases. 
Since it utilizes JDBC, data can be transferred between any kind database that provides a JDBC driver. 

    
### SQL tasks

SQL tasks are specified using XML files. 
SQL statements can be parameterized using the `${param_name}` notation. 
Parameter's types are defined in the `<parameters>`-element. 
The `type` attribute will be used to bind the value using the correct Java-type.
You can also just have ezDB replace values as String-literals. 
To do this, specify the type as `de.eztools.ezdb.api.NoBind`.

### Update task

```xml

<update name="Example 1">
    <statement><![CDATA[UPDATE my_table SET some_int_col    = ${some_int_param},
                                                 some_string_col = '${some_string_param}'
                             WHERE some_key = 5]]></statement>
    <parameters>
        <parameter name="some_int_param" type="java.lang.Integer"/>
        <parameter name="some_string_param" type="de.eztools.ezdb.api.model.NoBind"/>
    </parameters>
</update>
```   

### Copy task

```xml
    <copy name="Copy departments">
         <selectStatement><![CDATA[SELECT id, name, manager FROM department]]></selectStatement>
         <insertStatement><![CDATA[INSERT INTO backup.department (id, name, manager) VALUES (?, ?, ?)]]></insertStatement>
    </copy>
```  

### Import CSV task

For Supported formats see [Apache Commons CSV](https://commons.apache.org/proper/commons-csv/apidocs/org/apache/commons/csv/CSVFormat.Predefined.html)

```xml
    <importCsv name="Import departments">
         <fileName>./departments.csv</fileName>
         <format>Excel</format>
         <tableName>department</tableName>
    </importCsv>
```   

### Export CSV task

For Supported formats see [Apache Commons CSV](https://commons.apache.org/proper/commons-csv/apidocs/org/apache/commons/csv/CSVFormat.Predefined.html)

```xml
    <exportCsv name="Export departments">
         <fileName>./departments.csv</fileName>
         <format>Excel</format>
         <statement><![CDATA[SELECT id, name, manager FROM department]]></statement>
    </exportCsv>
```   

### Export binary task

Exports Blob data. For each row tín the result set a file will generated.

```xml
    <exportBinary name="Export departments">
         <fileNameColumnIndex>1</fileNameColumnIndex>
         <dataColumnIndex>2</dataColumnIndex>
         <filePrefix>C:\myFolder\</filePrefix>
         <fileSuffix>.png</fileSuffix>
         <statement><![CDATA[SELECT id, image from images]]></statement>
    </exportBinary>
```   

### Print task

This task prints the result of a query on the console:

```xml
    <print name="Print departments">
         <statement><![CDATA[SELECT id, name, manager FROM department]]></statement>
    </print>
```   
        
The result will look something like this:

    +---+-----------+-------------+
    |ID |NAME       |MANAGER      |
    +---+-----------+-------------+
    |1  |Accounting |Peter Smith  |
    +---+-----------+-------------+
    |2  |Sales      |Sandra White |
    +++++++++++++++++++++++++++++++
    
### Suite

Multiple tasks can be combined to suites in one single file:
```xml
    <suite name="Backup">
        <tasks>
            <update name="Drop backup table">
                <statement><![CDATA[DROP TABLE IF EXISTS backup.department CASCADE]]></statement>
            </update>
            <update name="Create backup table">
                <statement><![CDATA[CREATE TABLE backup.department (
                    id INTEGER,
                    name VARCHAR(40),
                    manager VARCHAR(40),
                    CONSTRAINT backup_department_pk PRIMARY KEY (id),
                )]]></statement>
            </update>
            <copy name="backup departments">
                <selectStatement><![CDATA[SELECT id, name, manager FROM hr.department]]></selectStatement>
                <insertStatement><![CDATA[INSERT INTO backup.department (id, name, manager) VALUES (?, ?, ?)]]></insertStatement>
            </copy>
        </tasks>
    </suite>
```    
## Command line interface

    ezdb:> help
        AVAILABLE COMMANDS
        
        Built-In Commands
              clear: Clear the shell screen.
              exit, quit: Exit the shell.
              help: Display help about available commands.
              history: Display or save the history of previously run commands
              script: Read and execute commands from a file.
              stacktrace: Display the full stacktrace of the last error.
        
        Ezdb Commands
              connect, connect-target: Create target database connection
              connect-source: Create source database connection (only needed for copy tasks)
            * copy: Copy data between databases
              disconnect: Close database connections
              drivers: List known drivers
            * execute: Execute task file
            * export-binary: Export binary data to file(s)
            * export-csv: Export data to a CSV file
            * import-csv: Import from CSV file
              parameters: List parameters
            * print: Execute select
              set-batch-size: Sets JDBC batch size
              set-fetch-size: Sets JDBC fetch size
              set-parameter: Sets a parameter that can be used in task files
              status: Print connection pool status
              unset-parameter: Remove parameter
            * update: Execute update


## Additional JDBC drivers

To be able to connect to any database, it's JDBC driver must be on the classpath. 
Add your database's driver to the `jdbc` directory and ezDB will pick it up.