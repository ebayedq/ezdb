package de.eztools.ezdb.api.model;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.*;

public interface Suite extends Task {

    List<Task> getTasks();

    @Override
    default Set<Parameter> getParameters() {
        return getTasks().stream()
                .map(Task::getParameters)
                .flatMap(Collection::stream)
                .collect(toSet());
    }

    @Override
    default void accept(TaskVisitor visitor) {
        for (Task task : getTasks()) {
            task.accept(visitor);
        }
    }

    @Override
    default List<Task> flatten() {
        return getTasks();
    }
}
