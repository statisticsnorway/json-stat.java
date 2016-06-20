package org.jsonstat.v2;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import me.yanaga.guava.stream.MoreCollectors;
import org.jsonstat.JsonStat;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Created by hadrien on 07/06/16.
 * https://json-stat.org/format/#dimension
 */
public class Dimension extends JsonStat {

    private final Category category;
    // https://json-stat.org/format/#label
    private String label;

    public Dimension(Category category) {
        super(Version.TWO, Class.DIMENSION);
        this.category = category;
    }

    public static Builder create(final String name) {
        return new Builder(name);
    }

    public Optional<String> getLabel() {
        // "label content should be written in lowercase except when it is a dataset label"
        return Optional.ofNullable(label).map(String::toUpperCase);
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Category getCategory() {
        return category;
    }

    enum Roles {
        TIME, GEO, METRIC
    }

    // https://json-stat.org/format/#category
    public static class Category {

        // TODO: Without label, index must be map.
        // TODO: When label is map, index must be a map.
        // TODO: When label is map, its keys must match those of index.
        // TODO: Index can be omitted if the dimension is constant.
        // TODO: If index is absent, label is required.

        // https://json-stat.org/format/#label
        // Optional, unless no index
        private ImmutableMap<String, String> label;

        // https://json-stat.org/format/#index
        // This can be Map or List. The order matters, and is linked to the
        // role of the dimension.
        // Optional if dimension is constant.
        private ImmutableSet<String> index;

        // TODO: Any key must be in the index.
        // TODO: If present, index should be a map
        // TODO: Values can be from the index, or from itself (index backed impl?)
        private Multimap<String, String> child;


        private Map<String, Coordinate> coordinates;

        // TODO: Only valid for dimension with metric role.
        // TODO: Implies that index is index is a map.
        private Map<String, String> unit;

        public ImmutableMap<String, String> getLabel() {
            return label;
        }

        public ImmutableSet<String> getIndex() {
            return index;
        }
    }

    // https://json-stat.org/format/#unit
    public static class Unit {

        // TODO: Documentation says, if unit is present, decimals is required?
        // https://json-stat.org/format/#decimals
        Integer decimals;

        // https://json-stat.org/format/#symbol
        String symbol;

        // https://json-stat.org/format/#position
        String position;

    }

    public static class Coordinate {
        final Double longitude;
        final Double latitude;

        public Coordinate(Double longitude, Double latitude) {
            this.longitude = longitude;
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public Double getLatitude() {
            return latitude;
        }
    }

    public static class Builder {

        private final String id;
        private final ImmutableSet.Builder<String> index;
        private final ImmutableMap.Builder<String, String> labels;

        private String label;
        private Roles role;

        private Builder(String id) {
            this.id = id;
            this.index = ImmutableSet.builder();
            this.labels = ImmutableMap.builder();
            // Use Dimension.create()
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Builder builder = (Builder) o;
            return Objects.equal(id, builder.id);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(id);
        }

        @Override
        public String toString() {
            return "DimensionBuilder{" +
                    "id='" + id + '\'' +
                    '}';
        }

        protected String getId() {
            return id;
        }

        // TODO: Should this be accessible at this stage? Maybe best to delay until dimension are build.
        protected Integer size() {
            return index.build().size();
        }

        public Builder withRole(final Roles role) {
            this.role = role;
            return this;
        }

        public Builder withLabel(final String label) {
            this.label = label;
            return this;
        }

        public Builder withCategories(String... categories) {
            return withCategories(ImmutableSet.copyOf(categories));
        }

        public Builder withCategories(ImmutableSet<String> categories) {
            Map<String, String> newIndexedLabels = categories.stream()
                    .collect(
                            MoreCollectors.toImmutableMap(
                                    Function.identity(),
                                    Function.identity()
                            )
                    );
            return withIndexedLabels(ImmutableMap.copyOf(newIndexedLabels));
        }

        public Builder withLabels(String... categories) {
            return withLabels(ImmutableList.copyOf(categories));
        }

        public Builder withLabels(ImmutableList<String> categories) {
            final Integer[] size = {labels.build().size()};
            Map<String, String> newIndexedLabels = categories.stream()
                    .collect(
                            MoreCollectors.toImmutableMap(s ->
                                            Integer.toString(size[0]++, 36),
                                    Function.identity()
                            )
                    );
            return withIndexedLabels(ImmutableMap.copyOf(newIndexedLabels));

        }

        /**
         * Set the values of the dimension in index/label form.
         *
         * @param indexedLabels
         * @return
         */
        public Builder withIndexedLabels(ImmutableMap<String, String> indexedLabels) {
            // TODO: index seems unnecessary, we could use index.keySet()
            index.addAll(indexedLabels.keySet());
            labels.putAll(indexedLabels);
            return this;
        }

        /**
         * Set GEO role.
         * <p>
         * Equivalent to {@code this.withRole(Roles.GEO);}
         *
         * @return the builder
         */
        public Builder withGeoRole() {
            return this.withRole(Roles.GEO);
        }

        /**
         * Set METRIC role.
         * <p>
         * Equivalent to {@code this.withRole(Roles.METRIC);}
         *
         * @return the builder
         */
        public Builder withMetricRole() {
            return this.withRole(Roles.METRIC);
        }

        /**
         * Set TIME role.
         * <p>
         * Equivalent to {@code this.withRole(Roles.TIME);}
         *
         * @return the builder
         */
        public Builder withTimeRole() {
            return this.withRole(Roles.TIME);
        }

        public Dimension build() {
            Category category = new Category();
            category.index = this.index.build();
            category.label = this.labels.build();
            Dimension dimension = new Dimension(category);
            dimension.setLabel(this.label);
            return dimension;
        }

        public ImmutableSet<String> getIndex() {
            return index.build();
        }

        protected boolean isMetric() {
            return Roles.METRIC.equals(this.getRole());
        }

        protected Roles getRole() {
            return this.role;
        }
    }
}
