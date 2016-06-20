package org.jsonstat.v1;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import org.jsonstat.v1.util.IntCartesianProduct;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Dataset {
    private final String id;
    private final Optional<String> label;
    private final List<Data> values;
    private final Optional<Instant> updated;
    private final int[] size;
    private final Map<String, Dimension> dimensions = new LinkedHashMap<>();
    private final Set<String> requiredDimensions;

    public Dataset(String id, Optional<String> label, List<Data> values, Optional<Instant> updated, List<Dimension> dimensions) {
        this(id, label, values, updated, toDimMap(dimensions));
    }

    public Dataset(String id, Optional<String> label, List<Data> values, Optional<Instant> updated, Map<String, Dimension> dimensions) {
        this.id = id;
        this.label = label;
        this.values = values;
        this.updated = updated;
        this.dimensions.putAll(dimensions);
        this.size = toSizes(dimensions);
        this.requiredDimensions = buildRequiredDimensionIds();
    }

    public String getId() {
        return id;
    }


    public Optional<String> getLabel() {
        return label;
    }

    public List<List<Data>> getRows(Dimension rowDimension) {
        IntCartesianProduct product = this.asCartasianProduct();
        List<int[]> asList = product.asList();

        int groupingIndex = rowDimension.getIndex();
        int lastIndex = 0;

        List<List<Data>> rows = new ArrayList<>();
        for (int i = 0; i < rowDimension.getSize(); i++) {
            List<Data> row = new ArrayList<>();
            for (int j = lastIndex; j < asList.size(); j++) {
                int[] coord = asList.get(j);
                int cv = coord[groupingIndex];
                if (cv == i) {
                    row.add(this.getValue(coord));
                } else if (cv == i + 1) {
                    lastIndex = j;
                    break;
                }
            }
            rows.add(row);
        }
        return rows;
    }

    public List<Data> getSlice(Map<String, String> dimensionCategories) {
        int[] dimensionIndices = getDimensionIndices(dimensionCategories);
        Set<String> missing = validateRequiredDimensions(dimensionCategories.keySet());
        List<Data> result = new ArrayList<>();
        if (missing.isEmpty()) {
            result.add(getValue(dimensionIndices));
        }
        else if (missing.size() == 1) {
            int missingIndex = indexOf(dimensionIndices, -1);
            String id = missing.iterator().next();
            Dimension dim = dimensions.get(id);

            for (int c = 0; c < dim.getSize(); c++) {
                int[] clone = dimensionIndices.clone();
                clone[missingIndex] = c;
                result.add(getValue(clone));
            }
        }
        else {
            throw new IllegalArgumentException("More than one dimension was missing: " + missing);
        }

        return result;
    }

    public Data getValue(Map<String, String> dimensionCategories) {
        Set<String> missing = validateRequiredDimensions(dimensionCategories.keySet());
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format(
                            "To get a single value, all dimensions must be present. %s is missing",
                            missing
                    )
            );
        }
        return getValue(getDimensionIndices(dimensionCategories));
    }

    public Data getValue(int[] dimensions) {
        return getValue(indexInCube(dimensions));
    }

    public Data getValue(int index) {
        return values.get(index);
    }

    public IntCartesianProduct asCartasianProduct() {
        return new IntCartesianProduct(size.clone());
    }

    public List<Data> getValues() {
        return values;
    }

    public Optional<Instant> getUpdated() {
        return updated;
    }

    public Optional<Dimension> getDimension(String id) {
        return Optional.ofNullable(dimensions.get(id));
    }

    public List<Dimension> getDimensions() {
        return ImmutableList.copyOf(dimensions.values());
    }

    public int size() {
        return values.size();
    }

    private int indexInCube(int[] dimension) {
        int mult = 1;
        int num = 0;
        for (int i = 0; i < size.length; i++) {
            mult *= (i > 0) ? size[size.length-i] : 1;
            num += mult * dimension[size.length-i-1];
        }
        return num;
    }

    private int indexOf(int[] arr, int value) {
        for (int i = 0; i < arr.length; i++) {
            int v = arr[i];
            if (v == value) {
                return i;
            }
        }
        return -1;
    }

    private int[] getDimensionIndices(Map<String, String> dimensionCategories) {
        int[] indices = new int[dimensions.size()];
        int index = 0;
        for (Map.Entry<String, Dimension> entry : dimensions.entrySet()) {
            Dimension dimension = entry.getValue();
            if (dimension.isConstant()) {
                indices[index] = 0;
            } else {
                String catID = dimensionCategories.get(entry.getKey());
                if (catID != null) {
                    indices[index] = dimension.getCategoryIndex(catID);
                }
                else {
                    indices[index] = -1;
                }
            }
            index++;
        }
        return indices;
    }

    private Set<String> validateRequiredDimensions(Set<String> ids) {
        return Sets.difference(requiredDimensions, ids);
    }

    private Set<String> buildRequiredDimensionIds() {

        // TODO Document better. As far as I can tell for now, this basically filters
        // TODO out the non required dimension ids.
        ImmutableList<Dimension> dimensions = ImmutableList.copyOf(this.dimensions.values());
        return dimensions.stream().flatMap(dimension -> {
            return dimension.isRequired() ? Stream.of(dimension.getId()) : Stream.of((String) null);
        }).collect(Collectors.toSet());

    }

    private int[] toSizes(Map<String, Dimension> dimensions) {
        int[] sizes = new int[dimensions.size()];
        int i = 0;
        for (Dimension dimension : dimensions.values()) {
            sizes[i] = dimension.getSize();
            i++;
        }
        return sizes;
    }

    private static Map<String, Dimension> toDimMap(Iterable<Dimension> dimensions) {
        LinkedHashMap<String, Dimension> map = new LinkedHashMap<>();
        for (Dimension dimension : dimensions) {
            map.put(dimension.getId(), dimension);
        }
        return map;
    }
}
