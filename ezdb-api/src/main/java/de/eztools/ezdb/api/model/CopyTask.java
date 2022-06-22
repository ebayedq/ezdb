package de.eztools.ezdb.api.model;

public interface CopyTask extends Task {

	String getSelectStatement();

	String getInsertStatement();

	@Override
	default void accept(TaskVisitor visitor) {
		visitor.visit(this);
	}
}
