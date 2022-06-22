package de.eztools.ezdb.shell.table;

import org.springframework.beans.BeanUtils;
import org.springframework.shell.table.ArrayTableModel;
import org.springframework.shell.table.BeanListTableModel;
import org.springframework.shell.table.BorderStyle;
import org.springframework.shell.table.TableModel;

import java.beans.PropertyDescriptor;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class EzTable implements TableRenderer, TableSource {

    private TableModel tableModel;
    private int rows;
    private boolean renderRowCount;

    private EzTable() {

    }

    public static TableSource builder() {
        return new EzTable();
    }

    @Override
    public <T> TableRenderer fromBean(Class<T> type, T t) {
        List<String> propertyNames = new ArrayList<>();
        for (PropertyDescriptor propertyName : BeanUtils.getPropertyDescriptors(type)) {
            if ("class".equals(propertyName.getName())) {
                continue;
            }
            propertyNames.add(propertyName.getName());
        }
        Map<String, Object> header = propertyNames.stream()
                .collect(Collectors.toMap(Function.identity(), Function.identity()));
        this.tableModel = new BeanListTableModel<>(Collections.singletonList(t), new LinkedHashMap<>(header));
        this.rows = 1;
        return this;
    }

    @Override
    public TableRenderer fromResultSet(ResultSet resultSet) throws SQLException {
        int columnCount = resultSet.getMetaData().getColumnCount();

        List<List<Object>> rows = new ArrayList<>();
        rows.add(new ArrayList<>());
        for (int col = 1; col <= columnCount; col++) {
            List<Object> header = rows.get(0);
            header.add(resultSet.getMetaData().getColumnLabel(col));
        }

        while (resultSet.next()) {
            List<Object> row = new ArrayList<>();
            for (int col = 1; col <= columnCount; col++) {
                row.add(resultSet.getObject(col));
            }
            rows.add(row);
        }

        tableModel = new TableModel() {
            @Override
            public int getRowCount() {
                return rows.size();
            }

            @Override
            public int getColumnCount() {
                return columnCount;
            }

            @Override
            public Object getValue(int row, int column) {
                return rows.get(row).get(column);
            }
        };
        this.rows = rows.size() - 1;

        return this;
    }

    @Override
    public TableRenderer fromArray(Object[][] data) {
        tableModel = new ArrayTableModel(data);
        rows = data.length - 1;
        return this;
    }

    @Override
    public String render() {
        org.springframework.shell.table.TableBuilder tableBuilder = new org.springframework.shell.table.TableBuilder(tableModel);
        String render = tableBuilder.addFullBorder(BorderStyle.fancy_light)
                .build()
                .render(Integer.MAX_VALUE);

        if (renderRowCount) {
            return render + "\n" + rows + " row(s)";
        }
        return render;
    }

    @Override
    public String renderWithRowCount() {
        renderRowCount = true;
        return render();
    }
}
