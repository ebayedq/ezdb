package de.eztools.ezdb.api.model;

public interface UpdateTask extends Task {

	String getStatement();
	
	@Override
	default void accept(TaskVisitor visitor) {
		visitor.visit(this);
	}
}
