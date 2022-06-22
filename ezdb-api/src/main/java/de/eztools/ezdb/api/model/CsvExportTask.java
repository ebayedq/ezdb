package de.eztools.ezdb.api.model;

public interface CsvExportTask extends Task {

	String getFileName();

	/**
	 * @link https://commons.apache.org/proper/commons-csv/apidocs/org/apache/commons/csv/CSVFormat.Predefined.html
	 */
	String getFormat();

	String getStatement();
	
	@Override
	default void accept(TaskVisitor visitor) {
		visitor.visit(this);
	}
}
