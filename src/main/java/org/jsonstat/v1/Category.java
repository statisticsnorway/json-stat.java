package org.jsonstat.v1;




import java.util.*;

public final class Category implements Iterable<String> {
    private final Map<String, String> labels = new LinkedHashMap<>();
    private final Map<String, Integer> indices = new LinkedHashMap<>();
    private final Map<String, List<String>> children = new LinkedHashMap<>();

    public Category(Map<String, Integer> indices, Map<String, String> labels, Map<String, List<String>> children) {
        this.indices.putAll(indices);
        this.labels.putAll(labels);
        this.children.putAll(children);
    }

    public int getIndex(String id) {
        Integer integer = indices.get(id);
        if (integer == null) {
            return 0;
        }
        return integer;
    }

    public Optional<String> getLabel(String id) {
        return Optional.ofNullable(labels.get(id));
    }

    public List<String> getChild(String id) {
        return children.containsKey(id) ? children.get(id) : Collections.<String>emptyList();
    }

    @Override
    public Iterator<String> iterator() {
        if (!indices.isEmpty()) {
            return indices.keySet().iterator();
        }
        else {
            return labels.keySet().iterator();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Category category = (Category) o;

        if (!children.equals(category.children)) return false;
        if (!indices.equals(category.indices)) return false;
        if (!labels.equals(category.labels)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = labels.hashCode();
        result = 31 * result + indices.hashCode();
        result = 31 * result + children.hashCode();
        return result;
    }
}

