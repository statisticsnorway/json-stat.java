package org.jsonstat.v1.table;


import java.util.Optional;

public final class TableHeader {
    private final Optional<String> id;
    private final Optional<String> label;

    public TableHeader(Optional<String> id) {
        this(id, id);
    }

    public TableHeader(Optional<String> id, Optional<String> label) {
        this.id = id;
        this.label = label != null ? label : id;
    }

    public Optional<String> getId() {
        return id;
    }

    public Optional<String> getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return "TableHeader{" +
                "id='" + id + '\'' +
                ", label='" + label + '\'' +
                '}';
    }
}
