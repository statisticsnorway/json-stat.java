package no.ssb.jsonstat.v2;

import com.codepoetics.protonpack.StreamUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import me.yanaga.guava.stream.MoreCollectors;
import no.ssb.jsonstat.JsonStat;
import no.ssb.jsonstat.v2.support.DatasetTableView;

import java.time.Instant;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A model of the JSON-stat dataset format.
 * <p>
 * This model is a java based implementation of the JSON-stat format defined at
 * <a href="https://json-stat.org/">json-stat.org/</a>. It relies heavily on Java 8 and the Google Guava library.
 * <p>
 * Instances of this class are immutable and must be created using the provided {@link Dataset#create()} static
 * method.
 */
public abstract class Dataset extends JsonStat {

    private final String label;
    private final String source;
    private final Instant updated;
    // TODO: Support for status.


    protected Dataset(String label, String source, Instant updated) {
        super(Version.TWO, Class.DATASET);
        this.label = label;
        this.source = source;
        this.updated = updated;
    }

    /**
     * Create a new {@link DatasetBuilder} instance.
     */
    public static DatasetBuilder create() {
        return new Builder();
    }

    /**
     * Create a new {@link DatasetBuilder} instance.
     */
    public static DatasetBuilder create(String label) {
        Builder builder = new Builder();
        return builder.withLabel(label);
    }

    /**
     * Return an {@link ImmutableSet} with the available dimensions in
     * the dataset, in order. It is consistent with {@link #getSize()}.
     *
     * @see <a href="https://json-stat.org/format/#id">json-stat.org/format/#id</a>
     */
    public ImmutableSet<String> getId() {
        return ImmutableSet.copyOf(getDimension().keySet());
    }

    /**
     * Return an {@link ImmutableMultimap} representing the roles of the dimensions.
     *
     * @see <a href="https://json-stat.org/format/#role">json-stat.org/format/#role</a>
     */
    public ImmutableMultimap<Dimension.Roles, String> getRole() {
        ImmutableMultimap.Builder<Dimension.Roles, String> builder;
        builder = ImmutableMultimap.builder();

        for (Map.Entry<String, Dimension> dimensionEntry : getDimension().entrySet()) {
            Dimension.Roles role = dimensionEntry.getValue().getRole();
            if (role != null) {
                builder.put(role, dimensionEntry.getKey());
            }
        }
        return builder.build();
    }

    /**
     * Return an {@link ImmutableList} with the size of the available dimensions in
     * the dataset, in order. It is consistent with {@link #getId()}.
     *
     * @see <a href="https://json-stat.org/format/#size">json-stat.org/format/#size</a>
     */
    public ImmutableList<Integer> getSize() {
        return getDimension()
                .values()
                .stream()
                .map(Dimension::getCategory)
                .map(Dimension.Category::getIndex)
                .map(AbstractCollection::size)
                .collect(MoreCollectors.toImmutableList());
    }

    /**
     * Return the extension value of this dataset.
     * <p>
     * If the dataset was deserialized, the return value will be an {@link ObjectNode}.
     *
     * @see <a href="https://json-stat.org/format/#size">json-stat.org/format/#extension</a>
     */
    @JsonProperty("extension")
    abstract Object getExtension();

    /**
     * Return the updated time of the dataset.
     *
     * @see <a href="https://json-stat.org/format/#updated">json-stat.org/format/#updated</a>
     */
    public Optional<Instant> getUpdated() {
        //  ISO 8601 format recognized by the Javascript Date.parse method (see ECMA-262 Date Time String Format).
        return Optional.ofNullable(updated);
    }

    /**
     * Return the label of the dataset.
     *
     * @see <a href="https://json-stat.org/format/#label">json-stat.org/format/#label</a>
     */
    public Optional<String> getLabel() {
        return Optional.ofNullable(label);
    }

    /**
     * Return the source of the dataset.
     *
     * @see <a href="https://json-stat.org/format/#source">json-stat.org/format/#source</a>
     */
    public Optional<String> getSource() {
        return Optional.ofNullable(source);
    }

    /**
     * Return the value sorted according to the dimensions of the dataset.
     *
     * @see <a href="https://json-stat.org/format/#value">json-stat.org/format/#value</a>
     */
    public abstract Map<Integer, Number> getValue();

    /**
     * Return the values as tuples.
     * <p>
     * The keys are the dimensions and values their associated values.
     */
    public abstract Map<List<String>, Number> asMap();

    /**
     * Return the values organized as a table.
     * <p>
     * Rows and columns are represented as a sets. For example, given the following dataset
     * with the dimensions A, B and C with 3, 2 and 4 categories respectively and the values:
     * <pre>
     *   A1B1C1   A1B1C2   A1B1C3   A1B1C4
     *   A1B2C1   A1B2C2   A1B2C3   A1B2C4
     *
     *   A2B1C1   A2B1C2   A2B1C3   A1B1C4
     *   A2B2C1   A2B2C2   A2B2C3   A2B2C4
     *
     *   A3B1C1   A3B1C2   A3B1C3   A3B1C4
     *   A3B2C1   A3B2C2   A3B2C3   A3B2C4
     * </pre>
     * <p>
     * Then calling this method with row A and C and column B will return the following table:
     * <p>
     * <pre>
     *              B1       B2
     *     A1,C1  A1B1C1   A1B2C1
     *     A1,C2  A1B1C2   A1B2C2
     *     A1,C3  A1B1C3   A1B2C3
     *     A1,C4  A1B1C4   A1B1C4
     *
     *     A2,C1  A2B1C1   A2B2C1
     *     A2,C2  A2B1C2   A2B2C2
     *     A2,C3  A2B1C3   A2B2C3
     *     A2,C4  A2B1C4   A2B1C4
     *
     *     A3,C1  A3B1C1   A3B2C1
     *     A3,C2  A3B1C2   A3B2C2
     *     A3,C3  A3B1C3   A3B2C3
     *     A3,C4  A3B1C4   A3B1C4
     * </pre>
     * <p>
     * Or with row A and column C and B:
     * <p>
     * <pre>
     *           B1       B1       B1       B1       B2       B2       B2       B2
     *           C1       C2       C3       C4       C1       C2       C3       C4
     *     A1  A1B1C1   A1B1C2   A1B1C3   A1B1C4   A1B2C1   A1B2C2   A1B2C3   A1B2C4
     *     A2  A2B1C1   A2B1C2   A2B1C3   A2B1C4   A2B2C1   A2B2C2   A2B2C3   A2B2C4
     *     A3  A3B1C1   A3B1C2   A3B1C3   A3B1C4   A3B2C1   A3B2C2   A3B2C3   A3B2C4
     * </pre>
     * <p>
     * Note that the returned {@link Table} is a view with a marginal overhead.
     *
     * @param row    the dimensions to use as rows.
     * @param column the dimensions to use as columns.
     * @throws IllegalArgumentException if a dimension is missing
     */
    public abstract Table<List<String>, List<String>, Number> asTable(Set<String> row, Set<String> column);

    /**
     * Return the dimensions of the dataset.
     *
     * @see Dimension
     * @see <a href="https://json-stat.org/format/#dimension">json-stat.org/format/#dimension</a>
     */
    public abstract Map<String, Dimension> getDimension();

    /**
     * Return the dimensions of the dataset.
     *
     * @see Dimension
     * @see <a href="https://json-stat.org/format/#dimension">json-stat.org/format/#dimension</a>
     */
    @JsonIgnore
    public Map<String, Dimension> getDimension(Collection<String> filter) {
        if (firstNonNull(filter, Collections.emptySet()).isEmpty())
            return Collections.emptyMap();

        return Maps.filterKeys(
                getDimension(),
                Predicates.in(filter)
        );
    }

    /**
     * Utility method that returns a {@link Iterable} of {@link List}s going through the data set
     * row by row and cell by cell, in the order defined by the dimensions.
     */
    @JsonIgnore
    public Collection<Number> getRows() {
        return getValue().values();
    }

    /**
     * A builder for dataset with defined dimensions.
     */
    static class ValuesBuilder implements DatasetValueBuilder {

        private final ImmutableMap<String, Dimension> dimensions;
        private final ImmutableList<List<String>> indexes;
        private final List<List<String>> indexProduct;

        private final String label;
        private final String source;
        private final Instant updated;

        private Object extension;

        ValuesBuilder(
                ImmutableSet<Dimension.Builder> dimensions,
                String label,
                String source,
                Instant updated,
                Object extension) {

            // Build the dimensions.
            this.dimensions = dimensions.stream()
                    .collect(MoreCollectors.toImmutableMap(
                            Dimension.Builder::getId,
                            Dimension.Builder::build
                    ));

            this.label = label;
            this.source = source;
            this.updated = updated;
            this.extension = extension;

            indexes = this.dimensions.values().stream()
                    .map(Dimension::getCategory)
                    .map(Dimension.Category::getIndex)
                    .map(ImmutableCollection::asList)
                    .collect(MoreCollectors.toImmutableList());

            indexProduct = Lists.cartesianProduct(indexes);
        }

        @Override
        public DatasetBuildable withValues(Collection<Number> values) {
            checkNotNull(values);

            if (values.isEmpty())
                return build(Stream.empty());

            return withValues(values.stream());
        }

        @Override
        public DatasetBuildable withValues(Iterable<Number> values) {
            checkNotNull(values);

            // Optimization.
            if (!values.iterator().hasNext())
                return build(Stream.empty());

            return withValues(StreamSupport.stream(
                    values.spliterator(),
                    false
            ));
        }

        @Override
        public DatasetBuildable withValues(Stream<Number> values) {
            checkNotNull(values);

            if (Stream.empty().equals(values))
                return build(Stream.empty());

            Stream<Map.Entry<Integer, Number>> entryStream = StreamUtils.zipWithIndex(values)
                    .map(tuple -> {
                        Integer dimensionIndex = Math.toIntExact(tuple.getIndex());
                        Number metric = tuple.getValue();
                        return new AbstractMap.SimpleEntry<>(
                                dimensionIndex, metric);
                    });

            return build(entryStream);
        }

        @Override
        public DatasetBuildable withMapper(Function<List<String>, Number> mapper) {
            // apply function and unroll.
            return withValues(indexProduct.stream().map(mapper));
        }

        @Override
        public ValuesBuilder addTuple(List<String> dimensions, Number value) {
            // TODO:
            return this;
        }

        public DatasetBuildable build(Stream<Map.Entry<Integer, Number>> entries) {

            Map<Integer, Number> values = entries.filter(entry -> entry.getValue() != null).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            return new DatasetBuildable() {
                @Override
                public Dataset build() {
                    return new Dataset(label, source, updated) {

                        @Override
                        Object getExtension() {
                            return extension;
                        }

                        @Override
                        public Map<Integer, Number> getValue() {
                            return values;
                        }

                        @Override
                        public Map<List<String>, Number> asMap() {
                            final Map<List<String>, Number> map = new AbstractMap<List<String>, Number>() {

                                @Override
                                public Number get(Object key) {
                                    int index = indexProduct.indexOf(key);
                                    if (index == -1)
                                        return null;

                                    return values.get(index);
                                }

                                @Override
                                public Set<Entry<List<String>, Number>> entrySet() {
                                    return new AbstractSet<Entry<List<String>, Number>>() {
                                        @Override
                                        public Iterator<Entry<List<String>, Number>> iterator() {
                                            return new Iterator<Entry<List<String>, Number>>() {

                                                ListIterator<List<String>> keyIterator = indexProduct.listIterator();

                                                @Override
                                                public boolean hasNext() {
                                                    return keyIterator.hasNext();
                                                }

                                                @Override
                                                public Entry<List<String>, Number> next() {
                                                    List<String> dims = keyIterator.next();
                                                    Number metric = values.get(keyIterator.previousIndex());
                                                    return new SimpleEntry<>(
                                                            dims,
                                                            metric
                                                    );
                                                }
                                            };
                                        }

                                        @Override
                                        public int size() {
                                            return values.size();
                                        }
                                    };
                                }
                            };
                            return map;
                        }

                        @Override
                        public Table<List<String>, List<String>, Number> asTable(Set<String> row, Set<String> column) {
                            return new DatasetTableView(this, row, column);
                        }

                        @Override
                        public Map<String, Dimension> getDimension() {
                            return dimensions;
                        }

                    };
                }
            };
        }

    }

    private static class Builder implements DatasetBuilder {

        private final ImmutableSet.Builder<Dimension.Builder> dimensionBuilders;
        private final ImmutableList.Builder<Optional<Number>> values;
        private Object extension;

        private String label;
        private String source;
        private Instant update;

        private Builder() {
            this.dimensionBuilders = ImmutableSet.builder();
            this.values = ImmutableList.builder();
        }

        @Override
        public DatasetBuilder withLabel(final String label) {
            this.label = checkNotNull(label, "label was null");
            return this;
        }

        @Override
        public DatasetBuilder withSource(final String source) {
            this.source = checkNotNull(source, "source was null");
            return this;
        }

        @Override
        public DatasetBuilder updatedAt(final Instant update) {
            this.update = checkNotNull(update, "updated was null");
            return this;
        }

        private DatasetBuilder addDimension(Dimension.Builder dimension) {
            checkNotNull(dimension, "the dimension builder was null");


            if (dimensionBuilders.build().contains(dimension))
                throw new DuplicateDimensionException(
                        String.format("the builder already contains the dimension %s", dimension.toString())
                );

            dimensionBuilders.add(dimension);
            return this;
        }

        /**
         * Assign a value to the extension.
         * <p>
         * The extension must be serializable by jackson.
         */
        @Override
        public Builder withExtension(Object extension) {
            this.extension = checkNotNull(extension);
            return this;
        }

        public Builder withDimension(Dimension.Builder dimension) {
            checkNotNull(dimension, "the dimension builder was null");

            if (dimensionBuilders.build().contains(dimension))
                throw new DuplicateDimensionException(
                        String.format("the builder already contains the dimension %s", dimension.toString())
                );

            dimensionBuilders.add(dimension);
            return this;
        }

        @Override
        public DatasetValueBuilder withDimensions(Iterable<Dimension.Builder> values) {
            checkNotNull(values, "dimension builder list was null");
            values.forEach(this::addDimension);
            return this.toValueBuilder();
        }

        @Override
        public DatasetValueBuilder withDimensions(Dimension.Builder... values) {
            checkNotNull(values, "dimension builder list was null");
            return this.withDimensions(Arrays.asList(values));
        }

        ValuesBuilder toValueBuilder() {
            return new ValuesBuilder(this.dimensionBuilders.build(), this.label, this.source, this.update, this.extension);
        }

    }
}
