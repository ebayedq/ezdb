package de.eztools.ezdb.api.model;

public interface PrintTask extends Task {

	String getStatement();
	
	@Override
	default void accept(TaskVisitor visitor) {
		visitor.visit(this);
	}
}
