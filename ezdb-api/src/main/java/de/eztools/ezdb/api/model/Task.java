package de.eztools.ezdb.api.model;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public interface Task {

	String getName();

	default String getParameterRegex() {
		return "\\$\\{([\\w\\d]+)\\}";
	}

	Set<Parameter> getParameters();

	void accept(TaskVisitor visitor);

	default List<Task> flatten() {
		return Collections.singletonList(this);
	}
}
