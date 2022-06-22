package de.eztools.ezdb.api.model;

import java.util.Collection;

public interface TaskVisitor {

    void visit(CopyTask task);

    void visit(UpdateTask task);

    void visit(CsvExportTask task);

    void visit(BinaryExportTask task);

    void visit(PrintTask task);

    void visit(ImportTask task);

    default void visitAll(Collection<? extends Task> tasks) {
        tasks.forEach(task -> task.accept(this));
    }


}
