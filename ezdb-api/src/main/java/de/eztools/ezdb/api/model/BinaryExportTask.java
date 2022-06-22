package de.eztools.ezdb.api.model;

public interface BinaryExportTask extends Task {

	int getFileNameColumnIndex();
	int getDataColumnIndex();

	String getFilePrefix();
	String getFileSuffix();

	String getStatement();
	
	@Override
	default void accept(TaskVisitor visitor) {
		visitor.visit(this);
	}

	default String makeFileName(String name) {
		return getFilePrefix() + name + getFileSuffix();
	}
}
