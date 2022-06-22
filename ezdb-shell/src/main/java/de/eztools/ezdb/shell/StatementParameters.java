package de.eztools.ezdb.shell;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
final class StatementParameters<T> {

    private static final Set<StatementParameters<?>> mappings = new HashSet<>();

    static {
        mappings.add(new StatementParameters<>(String.class, PreparedStatement::setString));
        mappings.add(new StatementParameters<>(Boolean.class, PreparedStatement::setBoolean));
        mappings.add(new StatementParameters<>(Byte.class, PreparedStatement::setByte));
        mappings.add(new StatementParameters<>(byte[].class, PreparedStatement::setBytes));

        // large objects
        mappings.add(new StatementParameters<>(Blob.class, PreparedStatement::setBlob));
        mappings.add(new StatementParameters<>(Clob.class, PreparedStatement::setClob));
        mappings.add(new StatementParameters<>(NClob.class, PreparedStatement::setNClob));

        // numbers
        mappings.add(new StatementParameters<>(Short.class, PreparedStatement::setShort));
        mappings.add(new StatementParameters<>(Long.class, PreparedStatement::setLong));
        mappings.add(new StatementParameters<>(Integer.class, PreparedStatement::setInt));
        mappings.add(new StatementParameters<>(Float.class, PreparedStatement::setFloat));
        mappings.add(new StatementParameters<>(Double.class, PreparedStatement::setDouble));
        mappings.add(new StatementParameters<>(BigDecimal.class, PreparedStatement::setBigDecimal));

        // time
        mappings.add(new StatementParameters<>(Date.class, PreparedStatement::setDate));
        mappings.add(new StatementParameters<>(Time.class, PreparedStatement::setTime));
        mappings.add(new StatementParameters<>(Timestamp.class, PreparedStatement::setTimestamp));
    }

    public static void apply(@NonNull PreparedStatement statement, int index, Object o) throws SQLException {
        if (o == null) {
            statement.setNull(index, Types.NULL);
        } else {

            Optional<StatementParameters<?>> mapping = mappings.stream()
                    .filter(m -> m.getType().isInstance(o))
                    .findAny();

            if (mapping.isPresent()) {
                mapping.get().accept(statement, index, o);
            } else {
                statement.setObject(index, o);
            }
        }
    }

    private final Class<T> type;
    private final ParameterConsumer<T> consumer;

    private void accept(PreparedStatement statement, int index, Object value) throws SQLException {
        consumer.accept(statement, index, type.cast(value));
    }

    private interface ParameterConsumer<V> {
        void accept(PreparedStatement statement, int index, V value) throws SQLException;
    }
}
