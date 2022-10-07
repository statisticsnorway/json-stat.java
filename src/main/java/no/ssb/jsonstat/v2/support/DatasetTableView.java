package no.ssb.jsonstat.v2.support;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.google.common.collect.UnmodifiableIterator;
import no.ssb.jsonstat.v2.Dataset;
import no.ssb.jsonstat.v2.Dimension;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An implementation of a {@link Table} that uses a {@link Dataset}
 * as data source.
 */
public class DatasetTableView implements Table<List<String>, List<String>, Number> {

    private final Dataset source;

    private final ImmutableSet<String> rows;
    private final ImmutableSet<String> columns;
    private final ImmutableMap<String, Integer> factors;
    private final ImmutableMap<String, ImmutableList<String>> dimensions;

    private final Set<List<String>> rowIndex;
    private final Set<List<String>> columnIndex;

    private final Integer size;


    public DatasetTableView(Dataset dataset, Set<String> rows, Set<String> colums) {
        this.source = checkNotNull(dataset, "dataset cannot be null");

        checkArgument(
                source.getDimension().keySet().equals(Sets.union(rows, colums)),
                "invalid row or column dimension names"
        );
        this.rows = ImmutableSet.copyOf(rows);
        this.columns = ImmutableSet.copyOf(colums);


        checkArgument(dataset.getId().size() == dataset.getSize().size());
        checkArgument(dataset.getId().size() >= 2, "need at least two dimensions to " +
                "represent as a table");

        ImmutableMap.Builder<String, Integer> factors = ImmutableMap.builder();

        UnmodifiableIterator<Integer> sizeIterator = dataset.getSize().reverse().iterator();
        UnmodifiableIterator<String> idIterator = dataset.getId().asList().reverse().iterator();

        factors.put(idIterator.next(), 1);

        Integer size = 1;
        while (sizeIterator.hasNext() && idIterator.hasNext()) {
            size *= sizeIterator.next();
            factors.put(idIterator.next(), size);
        }
        this.factors = factors.build();

        ImmutableMap.Builder<String, ImmutableList<String>> dimensions = ImmutableMap.builder();
        for (Map.Entry<String, Dimension> dimensionEntry : source.getDimension().entrySet()) {
            String dimensionName = dimensionEntry.getKey();
            ImmutableList<String> dimensionIndex = dimensionEntry.getValue().getCategory().getIndex().asList();
            dimensions.put(dimensionName, dimensionIndex);
        }
        this.dimensions = dimensions.build();

        this.rowIndex = computeIndex(rows);
        this.columnIndex = computeIndex(colums);

        this.size = source.getSize().stream().reduce(1, (a, b) -> a * b);
    }

    /**
     * Delegates to {@link Map#containsKey}. Returns {@code false} on {@code
     * ClassCastException} and {@code NullPointerException}.
     */
    static boolean safeContainsKey(Map<?, ?> map, Object key) {
        checkNotNull(map);
        try {
            return map.containsKey(key);
        } catch (ClassCastException | NullPointerException e) {
            return false;
        }
    }

    /**
     * Delegates to {@link Map#get}. Returns {@code null} on {@code
     * ClassCastException} and {@code NullPointerException}.
     */
    static <V> V safeGet(Map<?, V> map, Object key) {
        checkNotNull(map);
        try {
            return map.get(key);
        } catch (ClassCastException | NullPointerException e) {
            return null;
        }
    }

    private Set<List<String>> computeIndex(Set<String> dimensions) {
        List<Set<String>> rowDimensions = Lists.newArrayList();
        for (String row : dimensions) {
            rowDimensions.add(ImmutableSet.copyOf(this.dimensions.get(row)));
        }
        return Sets.cartesianProduct(rowDimensions);
    }

    @Override
    public boolean containsRow(Object rowKey) {
        return safeContainsKey(rowMap(), rowKey);
    }

    @Override
    public boolean containsColumn(Object columnKey) {
        return safeContainsKey(columnMap(), columnKey);
    }

    @Override
    public Set<List<String>> rowKeySet() {
        return rowIndex;
    }

    @Override
    public Set<List<String>> columnKeySet() {
        return columnIndex;
    }

    @Override
    public boolean containsValue(Object value) {
        for (Map<List<String>, Number> row : rowMap().values()) {
            if (row.containsValue(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean contains(Object rowKey, Object columnKey) {
        return get(rowKey, columnKey) != null;
    }

    @Override
    public Number get(Object rowKey, Object columnKey) {
        try {
            List<String> rowList = ((List<String>) rowKey);
            List<String> columnList = ((List<String>) columnKey);
            ImmutableList<String> rows = this.rows.asList();
            int index = 0;
            for (int i = 0; i < rows.size(); i++) {
                String key = rowList.get(i);
                String row = rows.get(i);
                index += dimensions.get(row).indexOf(key) * factors.get(row);
            }

            ImmutableList<String> columns = this.columns.asList();
            for (int i = 0; i < columns.size(); i++) {
                String key = columnList.get(i);
                String column = columns.get(i);
                index += dimensions.get(column).indexOf(key) * factors.get(column);
            }
            return source.getValue().get(index);
        } catch (ClassCastException e) {
            return null;
        }
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Map<List<String>, Number> row(List<String> rowKey) {
        return new AbstractMap<List<String>, Number>() {

            @Override
            public Set<Entry<List<String>, Number>> entrySet() {
                return new AbstractSet<Entry<List<String>, Number>>() {
                    @Override
                    public Iterator<Entry<List<String>, Number>> iterator() {
                        return Iterators.transform(
                                DatasetTableView.this.columnKeySet().iterator(),
                                columnKey -> {
                                    return new SimpleEntry<>(columnKey, DatasetTableView.this.get(rowKey, columnKey));
                                }
                        );
                    }

                    @Override
                    public int size() {
                        return DatasetTableView.this.columnKeySet().size();
                    }
                };
            }
        };
    }

    @Override
    public Map<List<String>, Number> column(List<String> columnKey) {
        return new AbstractMap<List<String>, Number>() {

            @Override
            public Set<Entry<List<String>, Number>> entrySet() {
                return new AbstractSet<Entry<List<String>, Number>>() {
                    @Override
                    public Iterator<Entry<List<String>, Number>> iterator() {
                        return Iterators.transform(
                                DatasetTableView.this.rowKeySet().iterator(),
                                rowKey -> {
                                    return new SimpleEntry<>(rowKey, DatasetTableView.this.get(rowKey, columnKey));
                                }
                        );
                    }

                    @Override
                    public int size() {
                        return DatasetTableView.this.rowKeySet().size();
                    }
                };
            }
        };
    }

    @Override
    public Set<Cell<List<String>, List<String>, Number>> cellSet() {
        Set<List<List<String>>> lists = Sets.cartesianProduct(rowKeySet(), columnKeySet());
        return lists.stream().map(dimensions -> {
            return Tables.immutableCell(dimensions.get(0), dimensions.get(1), get(dimensions.get(0), dimensions.get(1)));
        }).collect(Collectors.toSet());
    }

    @Override
    public Collection<Number> values() {
        return source.getRows();
    }

    @Override
    public Map<List<String>, Map<List<String>, Number>> rowMap() {
        return new AbstractMap<List<String>, Map<List<String>, Number>>() {
            @Override
            public Set<Entry<List<String>, Map<List<String>, Number>>> entrySet() {
                return new AbstractSet<Entry<List<String>, Map<List<String>, Number>>>() {
                    @Override
                    public Iterator<Entry<List<String>, Map<List<String>, Number>>> iterator() {
                        return Iterators.transform(
                                rowKeySet().iterator(),
                                rowKey -> {
                                    return new SimpleEntry<>(
                                            rowKey, DatasetTableView.this.row(rowKey)
                                    );
                                });
                    }

                    @Override
                    public int size() {
                        return rowKeySet().size();
                    }
                };
            }
        };
    }

    @Override
    public Map<List<String>, Map<List<String>, Number>> columnMap() {
        return new AbstractMap<List<String>, Map<List<String>, Number>>() {
            @Override
            public Set<Entry<List<String>, Map<List<String>, Number>>> entrySet() {
                return new AbstractSet<Entry<List<String>, Map<List<String>, Number>>>() {
                    @Override
                    public Iterator<Entry<List<String>, Map<List<String>, Number>>> iterator() {
                        return Iterators.transform(
                                columnKeySet().iterator(),
                                columnKey -> {
                                    return new SimpleEntry<>(
                                            columnKey, DatasetTableView.this.column(columnKey)
                                    );
                                });
                    }

                    @Override
                    public int size() {
                        return columnKeySet().size();
                    }
                };
            }
        };
    }

    /**
     * Guaranteed to throw an exception and leave the table unmodified.
     *
     * @throws UnsupportedOperationException always
     * @deprecated Unsupported operation.
     */
    @Deprecated
    @Override
    public final void clear() {
        throw new UnsupportedOperationException();
    }

    /**
     * Guaranteed to throw an exception and leave the table unmodified.
     *
     * @throws UnsupportedOperationException always
     * @deprecated Unsupported operation.
     */
    @Deprecated
    @Override
    public final Number put(List<String> rowKey, List<String> columnKey, Number value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Guaranteed to throw an exception and leave the table unmodified.
     *
     * @throws UnsupportedOperationException always
     * @deprecated Unsupported operation.
     */
    @Deprecated
    @Override
    public final void putAll(Table<? extends List<String>, ? extends List<String>, ? extends Number> table) {
        throw new UnsupportedOperationException();
    }

    /**
     * Guaranteed to throw an exception and leave the table unmodified.
     *
     * @throws UnsupportedOperationException always
     * @deprecated Unsupported operation.
     */
    @Deprecated
    @Override
    public final Number remove(Object rowKey, Object columnKey) {
        throw new UnsupportedOperationException();
    }


}
