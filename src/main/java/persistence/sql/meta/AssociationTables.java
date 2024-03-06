package persistence.sql.meta;

import java.util.List;
import java.util.stream.Collectors;

public class AssociationTables {
    private final List<AssociationTable> tables;

    public AssociationTables(List<AssociationTable> tables) {
        this.tables = tables;
    }

    public boolean isEmpty() {
        return tables.isEmpty();
    }

    public List<AssociationTable> getTables() {
        return tables;
    }

    public List<Column> getAllColumns() {
        return tables.stream()
                .map(AssociationTable::getColumns)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}
