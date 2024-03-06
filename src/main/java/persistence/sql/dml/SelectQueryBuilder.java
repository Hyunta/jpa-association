package persistence.sql.dml;

import persistence.sql.meta.AssociationTable;
import persistence.sql.meta.Column;
import persistence.sql.meta.DataType;
import persistence.sql.meta.IdColumn;
import persistence.sql.meta.Table;

import java.util.List;
import java.util.stream.Collectors;

public class SelectQueryBuilder {
    private static final String SELECT_QUERY_TEMPLATE = "SELECT %s FROM %s";
    private static final String JOIN_QUERY_TEMPLATE = " LEFT JOIN %s ON %s = %s";
    private static final String WHERE_CLAUSE_TEMPLATE = " WHERE %s = %s";
    private static final String COLUMN_DELIMITER = ", ";

    private static class InstanceHolder {
        private static final SelectQueryBuilder INSTANCE = new SelectQueryBuilder();
    }

    public static SelectQueryBuilder getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public String build(Class<?> target, Object id) {
        /**
         SELECT
         orders.id,
         orders.orderNumber,
         order_items.id,
         order_items.product,
         order_items.quantity
         FROM
         orders

         LEFT JOIN
         order_items
         ON
         orders.id = order_items.order_id
         WHERE
         orders.id = :orderId         */

        Table table = Table.from(target);
        String columnsNames = getColumnsNames(table.getColumns());

        if (table.containsAssociation()) {
            List<AssociationTable> associationTables = table.getAssociationTables();
            List<Column> associationColumns = table.getAssociationTablesColumns();
            String associationColumnNames = getColumnsNames(associationColumns);
            String selectQuery = String.format(SELECT_QUERY_TEMPLATE, columnsNames + associationColumnNames, table.getName());
            for (AssociationTable associationTable : associationTables) {
                String associationTableName = associationTable.getName();
                String tableIdName = table.getName() + "." + table.getIdColumn().getName();
                String joinTableName = associationTable.getName() + "." + associationTable.getJoinColumn();
                String joinQuery = String.format(JOIN_QUERY_TEMPLATE, associationTableName, tableIdName, joinTableName);
                selectQuery += joinQuery;
            }
            whereClause(table, id);
            return selectQuery + whereClause(table, id);
        }

        String selectQuery = String.format(SELECT_QUERY_TEMPLATE, columnsNames, table.getName());
        return selectQuery + whereClause(table, id);
    }

    private String getColumnsNames(List<Column> columns) {
        return columns.stream()
                .map(Column::getName)
                .collect(Collectors.joining(COLUMN_DELIMITER));
    }

    private String whereClause(Table table, Object id) {
        IdColumn idColumn = table.getIdColumn();
        String value = getDmlValue(id, idColumn);
        return String.format(WHERE_CLAUSE_TEMPLATE, idColumn.getName(), value);
    }

    private String getDmlValue(Object id, Column column) {
        DataType columnType = column.getType();
        if (columnType.isVarchar()) {
            return String.format("'%s'", id);
        }
        return id.toString();
    }
}
