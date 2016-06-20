package org.jsonstat.v2;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.*;
import me.yanaga.guava.stream.MoreCollectors;
import org.jsonstat.JsonStat;

import java.time.Instant;
import java.util.*;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A model of the JSON-stat dataset format.
 * <p>
 * This model is a java based implementation of the JSON-stat format defined on
 * <a href="https://json-stat.org/">json-stat.org/</a>. It relies heavily on Java 8 and the Google Guava library.
 * <p>
 * Instances of this class are immutable and must be created using the provided {@link Dataset#create(String)} static
 * method.
 */
public class Dataset extends JsonStat {

    // TODO: Use a set and transform to list.
    private final ImmutableSet<String> id;
    private final ImmutableList<Integer> size;

    private String label = null;
    private String source = null;
    private Instant updated = null;
    private Map<String, Dimension> dimension;
    private List<Object> value;

    protected Dataset(ImmutableSet<String> id, ImmutableList<Integer> size) {
        super(Version.TWO, Class.DATASET);
        checkArgument(id.size() == size.size(), "size and property sizes do not match");
        this.id = id;
        this.size = size;
    }

    /**
     * Create a new {@link Dataset.Builder} instance.
     */
    public static Builder create() {
        return new Builder();
    }

    /**
     * Create a new {@link Dataset.Builder} instance.
     */
    public static Builder create(String label) {
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
        // Cannot be empty. Should retain order.
        // Should be same order and size than id.
        return id;
    }

    /**
     * Return an {@link ImmutableList} with the size of the available dimensions in
     * the dataset, in order. It is consistent with {@link #getId()}.
     *
     * @see <a href="https://json-stat.org/format/#size">json-stat.org/format/#size</a>
     */
    public ImmutableList<Integer> getSize() {
        // Cannot be empty. Should retain order.
        // Should be same order and size than id.
        return size;
    }

    /**
     * Return the update time of the dataset.
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
    public Object getValue() {
        return value;
    }

    /**
     * Return the dimensions of the dataset.
     *
     * @see Dimension
     * @see <a href="https://json-stat.org/format/#dimension">json-stat.org/format/#dimension</a>
     */
    public Map<String, Dimension> getDimension() {
        return dimension;
    }

    /**
     * Utility method that returns a {@link Iterable} of {@link List}s going through the data set
     * row by row and cell by cell, in the order defined by the dimensions.
     */
    @JsonIgnore
    public Iterable<List<Object>> getRows() {
        return Iterables.paddedPartition(this.value, this.id.size());
    }

    /**
     * Utility method that returns a {@link Iterable} of {@link List}s going through the data set
     * row by row and cell by cell just as {@link #getRows()} does, but restrict the result to the
     * passed dimensions.
     * <p>
     * Note that although the order in which the cells are returned is defined by the passed
     * dimension names, the order of the rows is still defined by the dimensions.
     *
     * @param dimensions the dimension to return
     * @return list of rows
     * @throws IllegalArgumentException if any of the dimension names is not in the dataset
     * @throws NullPointerException     if any of the dimension names is null
     */
    @JsonIgnore
    public Iterable<List<Object>> getRows(String... dimensions) {
        return getRows(Arrays.asList(dimensions));
    }

    /**
     * Utility method that returns a {@link Iterable} of {@link List}s going through the data set
     * row by row and cell by cell just as {@link #getRows()} does, but restricts the result to the
     * given dimensions.
     * <p>
     * Note that although the order in which the cells are returned is defined by the given
     * dimension names, the order of the rows is still defined by the dimensions.
     *
     * @param dimensions the dimension to return
     * @return list of rows
     * @throws IllegalArgumentException if any of the dimension names is not in the dataset
     * @throws NullPointerException     if any of the dimension names is null
     */
    public Iterable<List<Object>> getRows(List<String> dimensions) {

        if (dimensions.isEmpty())
            return Collections.emptyList();

        if (dimensions.containsAll(this.id))
            return getRows();

        List<Boolean> index = Lists.newArrayList();
        for (String dimension : this.id.asList()) {
            if (dimensions.contains(dimension))
                index.add(true);
            else
                index.add(false);
        }

        return Iterables.transform(getRows(), input -> {
            ImmutableList.Builder<Object> filteredRow = ImmutableList.builder();
            for (int i = 0; i < index.size(); i++) {
                if (index.get(i))
                    filteredRow.add(input.get(i));
            }
            return filteredRow.build();
        });

    }

    public static class Builder {

        private final ImmutableSet.Builder<Dimension.Builder> dimensionBuilders;
        private final ImmutableList.Builder values;
        private String label;
        private String source;
        private Instant update;

        private Builder() {
            this.dimensionBuilders = ImmutableSet.builder();
            this.values = ImmutableList.builder();
        }

        public Builder withLabel(final String label) {
            this.label = checkNotNull(label, "label was null");
            return this;
        }

        public Builder withSource(final String source) {
            this.source = checkNotNull(source, "source was null");
            return this;
        }

        public Builder updatedAt(final Instant update) {
            this.update = checkNotNull(update, "updated was null");
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

        private ImmutableSet<Dimension> buildDimensions() {
            return ImmutableSet.copyOf(
                    Iterables.transform(dimensionBuilders.build(), Dimension.Builder::build)
            );
        }

        public Dataset build() {

            Map<String, Dimension> dimensionMap = Maps.transformValues(
                    Maps.uniqueIndex(dimensionBuilders.build(), Dimension.Builder::getId),
                    Dimension.Builder::build
            );

            // Optimized.
            ImmutableSet<String> ids = ImmutableSet.copyOf(dimensionMap.keySet());

            // TODO Make sure this is okay.
            ImmutableList<Integer> sizes = ImmutableList.copyOf(
                    Iterables.transform(dimensionBuilders.build(), Dimension.Builder::size)
            );

            Dataset dataset = new Dataset(ids, sizes);
            dataset.label = label;
            dataset.source = source;
            dataset.updated = update;
            dataset.value = values.build();
            dataset.dimension = dimensionMap;

            return dataset;
        }

        /**
         * Populate the data set with values.
         * <p>
         * The values are expected to be flattened in row-major order. See {@link Builder#withValues(Stream)} for a
         * details about row-major order.
         *
         * @param values the values in row-major order
         * @return a built data set
         */
        public Dataset withValues(java.util.Collection<Number> values) {
            checkNotNull(values);

            if (values.isEmpty())
                return withValues(Stream.empty());

            return withValues(values.stream());
        }

        /**
         * Populate the data set with values.
         * <p>
         * The values are expected to be flattened in row-major order. See {@link Builder#withValues(Stream)} for a
         * details about row-major order.
         *
         * @param values the values in row-major order
         * @return a built data set
         */
        public Dataset withValues(Iterable<Number> values) {
            checkNotNull(values);

            // Optimization.
            if (!values.iterator().hasNext())
                return withValues(Stream.empty());

            return withValues(StreamSupport.stream(
                    values.spliterator(),
                    false
            ));
        }

        /**
         * Populate the data set with values.
         * <p>
         * The values are expected to be flattened in row-major order. For example if we have three dimensions
         * (A, B and C) with 3, 2 and 4 categories respectively, the values should be ordered iterating first by the 4
         * categories of C, then by the 2 categories of B and finally by the 3 categories of A:
         * <p>
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
         *
         * @param values the values in row-major order
         * @return a built data set
         */
        public Dataset withValues(Stream<Number> values) {
            checkNotNull(values);

            Dataset dataset = this.build();

            // TODO: Does it make sense to create an empty data set?
            if (Stream.empty().equals(values))
                dataset.value = Collections.emptyList();
            else
                dataset.value = values.collect(MoreCollectors.toImmutableList());

            return dataset;
        }

        /**
         * Use a mapper function to populate the metrics in the data set.
         * <p>
         * The mapper function will be called for every combination of dimensions.
         *
         * @param mapper a mapper function to use to populate the metrics in the data set
         * @return the data set
         */
        public Dataset withMapper(Function<List<String>, List<Number>> mapper) {

            // Get all the dimensions.
            List<ImmutableList<String>> dimensions = dimensionBuilders.build().stream()
                    .filter(dimension -> !dimension.isMetric())
                    .map(dimension -> dimension.getIndex().asList())
                    // TODO: Find out what the order should be here.
                    //.sorted(Comparator.comparingInt(AbstractCollection::size))
                    .collect(MoreCollectors.toImmutableList());

            List<List<String>> combinations = Lists.cartesianProduct(dimensions);

            // apply function and unroll.
            return withValues(combinations.stream().map(mapper).flatMap(Collection::stream));
        }

        public Builder addRow(ImmutableMap<String, ?> row) {

            for (Dimension.Builder dimension : dimensionBuilders.build()) {
                String id = dimension.getId();

                // Save the dimension values.
                if (!dimension.isMetric())
                    dimension.withCategories(row.get(id).toString());
                // Add the value.
                values.add(row.get(id));

            }

            return this;

        }
    }
}
