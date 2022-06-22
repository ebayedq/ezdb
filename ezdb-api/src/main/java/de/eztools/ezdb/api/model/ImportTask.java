package de.eztools.ezdb.api.model;

public interface ImportTask extends Task {

	String getFileName();

	/**
     * @link https://commons.apache.org/proper/commons-csv/apidocs/org/apache/commons/csv/CSVFormat.Predefined.html
	 */
	String getFormat();

	String getTableName();
	
	@Override
	default void accept(TaskVisitor visitor) {
		visitor.visit(this);
	}
}
