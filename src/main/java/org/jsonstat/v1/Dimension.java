package org.jsonstat.v1;


import com.google.common.collect.Lists;

import java.util.*;

public final class Dimension {
    private int index;
    private String id;
    private Optional<String> label;
    private int size;
    private Category category;
    private Optional<Role> role;

    public Dimension(int index, String id, int size, Optional<String> label, Category category, Optional<Role> role) {
        this.index = index;
        this.id = id;
        this.size = size;
        this.label = label;
        this.category = category;
        this.role = role;
    }

    public int getIndex() {
        return index;
    }

    public boolean isConstant() {
        return size == 1;
    }

    public boolean isRequired() {
        return size > 1;
    }

    public int getCategoryIndex(String catID) {
        return category.getIndex(catID);
    }

    public int getSize() {
        return size;
    }

    public String getId() {
        return id;
    }

    public Optional<String> getLabel() {
        return label;
    }

    public Category getCategory() {
        return category;
    }

    public Map<String, List<String>> getCategoriesAsMap() {
        List<String> value = Lists.newArrayList(category);
        return Collections.singletonMap(getId(), value);
    }

    public Optional<Role> getRole() {
        return role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Dimension dimension = (Dimension) o;

        if (size != dimension.size) return false;
        if (!category.equals(dimension.category)) return false;
        if (!id.equals(dimension.id)) return false;
        if (!label.equals(dimension.label)) return false;
        if (!role.equals(dimension.role)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + label.hashCode();
        result = 31 * result + size;
        result = 31 * result + category.hashCode();
        result = 31 * result + role.hashCode();
        return result;
    }
}
