package de.eztools.ezdb.api.shell;

import de.eztools.ezdb.api.model.Parameter;
import de.eztools.ezdb.api.model.TaskVisitor;

import java.util.Map;

public interface TaskExecutor extends TaskVisitor {

    int DEFAULT_FETCH_SIZE = 1000;
    int DEFAULT_BATCH_SIZE = 1000;

    void setBatchSize(int batchSize);

    default int getBatchSize() {
        return DEFAULT_BATCH_SIZE;
    }

    void setFetchSize(int fetchSize);

    default int getFetchSize() {
        return DEFAULT_FETCH_SIZE;
    }

    Map<Parameter, Object> getParameters();

    void setParameters(Map<Parameter, Object> parameters);
}
