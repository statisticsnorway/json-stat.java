package org.jsonstat.v1.table;

import com.google.common.collect.Lists;
import org.jsonstat.v1.Category;
import org.jsonstat.v1.Data;
import org.jsonstat.v1.Dataset;
import org.jsonstat.v1.Dimension;
import org.jsonstat.v1.util.IntCartesianProduct;

import java.util.*;
import java.util.stream.StreamSupport;

import static org.jsonstat.v1.util.CollectionUtils.join;
import static org.jsonstat.v1.util.CollectionUtils.product;

public final class Table {
    private Optional<String> title;
    private final List<TableHeader> headers = new ArrayList<>();
    private final List<List<Data>> rows = new ArrayList<>();

    public Table(Optional<String> title, List<TableHeader> headers, List<List<Data>> rows) {
        this.title = title;
        this.headers.addAll(headers);
        for (List<Data> row : rows) {
            this.rows.add(Lists.newArrayList(row));
        }
    }

    public static Table fromDataset(Dataset dataset) {
        return fromDataset(dataset, findRowDimension(dataset));
    }

    public static Table fromDataset(Dataset dataset, Dimension rowDimension) {
        List<Dimension> dimensions = dataset.getDimensions();

        List<TableHeader> headers = buildHeader(dimensions, rowDimension.getId());
        List<List<Data>> rows = dataset.getRows(rowDimension);
        //TODO: maybe this should really be part of dataset.getRows()...
        int i = 0;
        for (String s : rowDimension.getCategory()) {
            List<Data> row = rows.get(i);
            int j = 0;
            row.add(j, new Data(rowDimension.getCategory().getLabel(s).orElse(s), Optional.<String>empty()));
            for (Dimension dimension : dimensions) {
                if (dimension.isConstant()) {
                    boolean added = false;
                    for (String id : dimension.getCategory()) {
                        row.add(j, new Data(dimension.getCategory().getLabel(id).orElse(id), Optional.<String>empty()));
                        added = true;
                    }
                    if (!added) {
                        row.add(j, new Data(dimension.getLabel().orElse(dimension.getId()), Optional.<String>empty()));
                    }
                    j++;
                }
            }
            i++;
        }

        return new Table(dataset.getLabel(), headers, rows);
    }

    private static List<TableHeader> buildHeader(List<Dimension> dimensions, String rowDimension) {
        //TODO: This is stupid. Fix it.
        List<List<String>> categories = new ArrayList<>();
        List<TableHeader> headers = new ArrayList<>();
        for (Dimension dimension : dimensions) {
            boolean isRow = rowDimension.equals(dimension.getId());
            if (dimension.isRequired() && !isRow) {
                Category category = dimension.getCategory();
                List<String> cats = new ArrayList<>();
                for (String id : category) {
                    cats.add(category.getLabel(id).orElse(id));
                }
                categories.add(cats);
            }
            else if (dimension.isConstant()) {
                Optional<String> dimensionId = StreamSupport.stream(dimension.getCategory().spliterator(), false)
                        .findFirst();
                headers.add(new TableHeader(dimensionId, dimension.getLabel()));
            }
            if (isRow) {
                headers.add(new TableHeader(Optional.<String>empty(), dimension.getLabel()));
            }
        }

        List<String[]> combinations = product(categories);

        for (String[] combination : combinations) {
            String label = join(Arrays.asList(combination), " ");
            headers.add(new TableHeader(Optional.<String>empty(), Optional.of(label)));
        }

        return headers;
    }


    private static Dimension findRowDimension(Dataset ds) {
        IntCartesianProduct p = ds.asCartasianProduct();
        return ds.getDimensions().get(p.getMaxIndex());
    }


    public Optional<String> getTitle() {
        return title;
    }

    public TableHeader getHeader(int index) {
        return headers.get(index);
    }

    public TableHeader getHeader(String id) {
        return getHeader(getHeaderIndex(id));
    }

    public int getHeaderIndex(String id) {
        for (int i = 0; i < headers.size(); i++) {
            TableHeader h = headers.get(i);
            if (h.getId().equals(Optional.of(id))) {
                return i;
            }
        }
        return -1;
    }

    public List<TableHeader> getHeaders() {
        return headers;
    }

    public List<Data> getRow(int index) {
        if (index < rows.size()) {
            return rows.get(index);
        }
        return Collections.emptyList();
    }

    public List<List<Data>> getRows() {
        return rows;
    }

    public <A> A render(Renderer<A> renderer) {
        return renderer.render(this);
    }
}
