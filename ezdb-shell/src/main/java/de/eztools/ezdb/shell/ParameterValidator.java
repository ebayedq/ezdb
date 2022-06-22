package de.eztools.ezdb.shell;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.eztools.ezdb.api.model.BinaryExportTask;
import de.eztools.ezdb.api.model.ImportTask;
import de.eztools.ezdb.api.model.PrintTask;
import de.eztools.ezdb.api.model.UpdateTask;
import de.eztools.ezdb.api.model.CopyTask;
import de.eztools.ezdb.api.model.CsvExportTask;
import de.eztools.ezdb.api.model.Parameter;
import de.eztools.ezdb.api.model.TaskVisitor;

final class ParameterValidator implements TaskVisitor {

	private final Map<Parameter, Object> parameterValues;
	
	private final Set<Parameter> missingParameters = new HashSet<>();
	
	public ParameterValidator(Map<Parameter, Object> parameterValues) {
		this.parameterValues = parameterValues;
	}

	@Override
	public void visit(CopyTask task) {
		validateParameters(task.getParameters());
	}

	@Override
	public void visit(UpdateTask task) {
		validateParameters(task.getParameters());
	}

	@Override
	public void visit(CsvExportTask task) {
		validateParameters(task.getParameters());
	}
	
	@Override
	public void visit(PrintTask task) {
		validateParameters(task.getParameters());
	}

	@Override
	public void visit(ImportTask task) {
		validateParameters(task.getParameters());
	}

	@Override
	public void visit(BinaryExportTask task) {
		validateParameters(task.getParameters());
	}

	private void validateParameters(Set<? extends Parameter> parameters) {
		Set<Parameter> missing = new HashSet<>(parameters);
		missing.removeAll(parameterValues.keySet());
		missingParameters.addAll(missing);
	}
	
	public boolean hasMissingParameters() {
		return !missingParameters.isEmpty();
	}
	
	public Set<Parameter> getMissingParameters() {
		return missingParameters;
	}

}
